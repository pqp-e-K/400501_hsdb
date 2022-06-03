package systems.pqp.hsdb.dao;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.Config;
import systems.pqp.hsdb.DataExtractor;
import systems.pqp.hsdb.DataHarmonizer;
import systems.pqp.hsdb.DataHarmonizerException;
import systems.pqp.hsdb.ImportException;
import systems.pqp.hsdb.dao.coreapi.V2ApiEpisode;
import systems.pqp.hsdb.dao.coreapi.V2ApiPage;
import systems.pqp.hsdb.dao.graphql.GraphQLGrouping;
import systems.pqp.hsdb.dao.graphql.GraphQLNode;
import systems.pqp.hsdb.types.RadioPlayType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class AudiothekDaoV2 {
    /**
     *
     */

    private static final Logger LOG = LogManager.getLogger(AudiothekDaoV2.class.getName());
    private static final Config CONFIG = Config.Config();
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    static final String API_URL = CONFIG.getProperty("api.v2.url");
    static final String RESOURCE = CONFIG.getProperty("api.v2.resource");
    static final String PROXY_HOST = CONFIG.getProperty("api.v2.proxy.url", null);
    static final String PROXY_PORT = CONFIG.getProperty("api.v2.proxy.port", null);
    static final String LIMIT = CONFIG.getProperty("api.v2.limit","100");
    static final String GRAPH_QL_URL = "https://api.ardaudiothek.de/graphql";
    static final String HEADER_ACCEPT_CONTENT_TYPE = "application/json";
    static final String[] AUDIOTHEK_EXCLUDES = CONFIG.getProperty(Config.AUDIOTHEK_EXCLUDES,"").split(",");

    String userPass = "deliver:J9Xsbzg4SkHvjvrgpx*c";
    String authorizationString = "Basic " + Base64.encodeBase64String(userPass.getBytes(StandardCharsets.UTF_8));

    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();

    /**
     *
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays() throws ImportException, InterruptedException {
        // get all shows
        List<GraphQLGrouping> graphQLShows = getRadioPlayShowsFromGraphQL();

        // Remove "Lesungen" id: 7258744, 9839150, 47077138, 55964050, 78907202, 93466914
        LOG.info("Entferne Lesungen [{}]",List.of(AUDIOTHEK_EXCLUDES));
        graphQLShows = graphQLShows.stream().filter(
                show -> {
                    for( String id: AUDIOTHEK_EXCLUDES ){
                        if( show.getId().equals(id) ){
                            return false;
                        }
                    }
                    return true;
                }
        ).collect(Collectors.toList());

        //String v2ApiRequestUrl = API_URL + "/items/publications/";

        Map<String, GenericObject> radioPlays = new HashMap<>();

        graphQLShows.forEach(
                show -> Arrays.stream(show.getItems().getEdges()).sequential().forEach(
                        edge -> radioPlays.put(edge.getNode().getId(), radioPlayFromApiResults(edge.getNode()))
                )
        );

        return radioPlays;
    }

    /**
     *
     * @param graphQLNode
     * @return
     */
    GenericObject radioPlayFromApiResults(GraphQLNode graphQLNode){
        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,graphQLNode.getId());

        HashSet<String> titles = new HashSet<>();
        String title = graphQLNode.getTitle();
        titles.add(title);

        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, new ArrayList<>(titles));
        radioPlay.addDescriptionProperty(RadioPlayType.LINK, graphQLNode.getSharingUrl());

        if( null == graphQLNode.getGraphQLDocument() ){
            LOG.warn("Node ohne Document! Id: {}", graphQLNode.getId() );
            return radioPlay;
        }

        //Episodennummer aus Titel lesen
        Integer episode;
        if( null == graphQLNode.getGraphQLDocument().getEpisodeNumber()) {
            episode = DataExtractor.getEpisodeFromTitle(title);
        } else {
            episode = graphQLNode.getGraphQLDocument().getEpisodeNumber();
        }
        if(episode != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.EPISODE, String.valueOf(episode));
        }

        //Staffelnummer aus Titel lesen
        Integer season = DataExtractor.getSeasonFromTitle(title);
        if(season != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.SEASON, String.valueOf(season));
        }

        if( null != graphQLNode.getGraphQLDocument().getDuration() ) {
            Integer duration = graphQLNode.getGraphQLDocument().getDuration();
            radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(duration));
        }

        try {
            if( null != graphQLNode.getGraphQLDocument().getStartDate() ) radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(graphQLNode.getGraphQLDocument().getStartDate()));
            if( null != graphQLNode.getGraphQLDocument().getStartDate() ) radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(graphQLNode.getGraphQLDocument().getStartDate()));
        } catch (DataHarmonizerException e) {
            LOG.warn(e.getMessage(), e);
        }

        if( null != graphQLNode.getGraphQLDocument().getDuration() ) radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(graphQLNode.getGraphQLDocument().getDuration()));
        if( null != graphQLNode.getGraphQLDocument().getProducer() ) radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, graphQLNode.getGraphQLDocument().getProducer());
        if( null != graphQLNode.getGraphQLDocument().getDescription() ) radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, graphQLNode.getGraphQLDocument().getDescription());

        return radioPlay;
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    List<GraphQLGrouping> getRadioPlayShowsFromGraphQL() throws InterruptedException, ImportException {

        LOG.info("Get radio plays from GraphQL...");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        HttpRequest.BodyPublisher body;
        try {
            body = HttpRequest.BodyPublishers.ofFile(
                    Path.of(Objects.requireNonNull(loader.getResource("graphqlrequest_4.json")).getFile()));
            HttpClient httpClient  = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GRAPH_QL_URL))
                    .header("Content-Type",HEADER_ACCEPT_CONTENT_TYPE)
                    .header("Accept",HEADER_ACCEPT_CONTENT_TYPE)
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .POST(body)
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                String responseAsString = new String(gzipDecompress(response.body()), StandardCharsets.UTF_8);

                Map<String, Object> responseMap = gson.fromJson(responseAsString, Map.class);
                String programSetListString = gson.toJson(
                        ((Map<String,Object>)((Map<String, Object>)responseMap
                                .get("data"))
                                .get("editorialCategory"))
                                .get("programSetsList")
                );
                return Arrays.asList(gson.fromJson(programSetListString, GraphQLGrouping[].class));
            } else {
                throw new ImportException("Get readio plays from GraphQL failed! " + response.statusCode());
            }
        } catch (IOException exception){
            throw new ImportException(exception.getMessage(), exception);
        }
    }

    /**
     *
     * @param compressedBytes
     * @return
     * @throws IOException
     */
    private byte[] gzipDecompress(byte[] compressedBytes) throws IOException {
        try (InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes))) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                inputStream.transferTo(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

}
