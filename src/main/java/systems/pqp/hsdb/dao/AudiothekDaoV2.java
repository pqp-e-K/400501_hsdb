package systems.pqp.hsdb.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.Config;
import systems.pqp.hsdb.DataExtractor;
import systems.pqp.hsdb.DataHarmonizer;
import systems.pqp.hsdb.DataHarmonizerException;
import systems.pqp.hsdb.ImportException;
import systems.pqp.hsdb.dao.graphql.GraphQLRadioPlayShowGrouping;
import systems.pqp.hsdb.dao.graphql.GraphQLNode;
import systems.pqp.hsdb.dao.graphql.GraphQLPublisherGrouping;
import systems.pqp.hsdb.types.RadioPlayType;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class AudiothekDaoV2 {
    /**
     *
     */

    private static final Logger LOG = LogManager.getLogger(AudiothekDaoV2.class.getName());
    private static final Config CONFIG = Config.Config();
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    static final String GRAPH_QL_URL = "https://api.ardaudiothek.de/graphql";
    static final String HEADER_ACCEPT_CONTENT_TYPE = "application/json";
    static final String[] AUDIOTHEK_EXCLUDES = CONFIG.getProperty(Config.AUDIOTHEK_EXCLUDES,"").split(",");

    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();

    /**
     *
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays() throws ImportException, InterruptedException {

        //build Lookup HashMap for Publisher/Rundfunkanstalten
        List<GraphQLPublisherGrouping> publisher = getPublisherFromGraphQL();
        HashMap<String,GraphQLPublisherGrouping> publisherLookUp = new HashMap<>();
        for(GraphQLPublisherGrouping publisherGrouping: publisher) {
            publisherLookUp.put(publisherGrouping.getCoreId(),publisherGrouping);
        }

        // get all shows
        List<GraphQLRadioPlayShowGrouping> graphQLShows = getRadioPlayShowsFromGraphQL();

        //String v2ApiRequestUrl = API_URL + "/items/publications/";
        Map<String, GenericObject> radioPlays = new HashMap<>();

        graphQLShows.forEach(
                show -> Arrays.stream(show.getItems().getEdges()).sequential().forEach(
                        edge -> {
                            GenericObject genericObject = radioPlayFromApiResults(edge.getNode(), publisherLookUp);
                            if( null != genericObject ) { // null wenn kein coreDocument oder keine duration
                                radioPlays.put(edge.getNode().getId(), genericObject);
                            }
                        }
                )
        );

        return radioPlays;
    }

    /**
     *
     * @param graphQLEpisode
     * @return
     */
    GenericObject radioPlayFromApiResults(GraphQLNode graphQLEpisode, HashMap<String,GraphQLPublisherGrouping> publisherLookUp){
        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,graphQLEpisode.getId());

        HashSet<String> titles = new HashSet<>();
        String title = graphQLEpisode.getTitle();
        titles.add(title);

        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, new ArrayList<>(titles));
        radioPlay.addDescriptionProperty(RadioPlayType.LINK, graphQLEpisode.getSharingUrl());

        if( null == graphQLEpisode.getGraphQLDocument() ){
            LOG.warn("Node ohne Document! Id: {}", graphQLEpisode.getId() );
            return null;
        }

        if( null == graphQLEpisode.getGraphQLDocument().getDuration() ) {
            LOG.warn("Document ohne Duration! Id: {}", graphQLEpisode.getId() );
            return null;
        }

        if( Boolean.parseBoolean(CONFIG.getProperty(Config.AUDIOTHEK_EXCLUDE_UNPUBLISHED,"true")) && !graphQLEpisode.getGraphQLDocument().getPublished() ){
            LOG.log(Level.DEBUG, "Ignoriere nicht veröffentlichte Episode.");
            return null;
        }
        Integer duration = graphQLEpisode.getGraphQLDocument().getDuration();
        radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(duration));

        //Episodennummer aus Titel lesen
        Integer episode;
        if( null == graphQLEpisode.getGraphQLDocument().getEpisodeNumber()) {
            episode = DataExtractor.getEpisodeFromTitle(title);
        } else {
            episode = graphQLEpisode.getGraphQLDocument().getEpisodeNumber();
        }
        if(episode != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.EPISODE, String.valueOf(episode));
        }

        //Staffelnummer aus Titel lesen
        Integer season = DataExtractor.getSeasonFromTitle(title);
        if(season != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.SEASON, String.valueOf(season));
        }

        try {
            if( null != graphQLEpisode.getGraphQLDocument().getStartDate() ) radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(graphQLEpisode.getGraphQLDocument().getStartDate()));
            if( null != graphQLEpisode.getGraphQLDocument().getStartDate() ) radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(graphQLEpisode.getGraphQLDocument().getStartDate()));
        } catch (DataHarmonizerException e) {
            LOG.warn(e.getMessage(), e);
        }

        if( null != graphQLEpisode.getGraphQLDocument().getDuration() ) radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(graphQLEpisode.getGraphQLDocument().getDuration()));
        if( null != graphQLEpisode.getGraphQLDocument().getProducer() ) {
            GraphQLPublisherGrouping publisherGrouping = publisherLookUp.get(graphQLEpisode.getGraphQLDocument().getPublisherId());
            radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, publisherGrouping.getOrganizationName());
        }
        if( null != graphQLEpisode.getGraphQLDocument().getDescription() ) radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, graphQLEpisode.getGraphQLDocument().getDescription());

        return radioPlay;
    }

    /**
     *
     * @return
     * @throws ImportException
     * @throws InterruptedException
     */
    public List<String> getUnpublishedRadioPlayIds() throws ImportException, InterruptedException {
        List<GraphQLRadioPlayShowGrouping> graphQLShows = getRadioPlayShowsFromGraphQL();
        List<String> graphQLEpisodeIds = new ArrayList<>();
        graphQLShows.forEach(
                show -> Arrays.stream(show.getItems().getEdges()).sequential().forEach(
                        edge -> {
                            if(null == edge.getNode().getGraphQLDocument() || !edge.getNode().getGraphQLDocument().getPublished()){
                                graphQLEpisodeIds.add(edge.getNode().getId());
                            }
                        }
                )
        );
        LOG.info("Nicht veröffentlichte Hörspiele in Audiothek: {}", graphQLEpisodeIds.size());
        return graphQLEpisodeIds;
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    List<GraphQLRadioPlayShowGrouping> getRadioPlayShowsFromGraphQL() throws InterruptedException, ImportException {

        LOG.info("Lese Hörspiele aus GraphQL...");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        HttpRequest.BodyPublisher body;
        try {
            InputStream inputStream = loader.getResourceAsStream("graphqlrequest_shows.json");
            body = HttpRequest.BodyPublishers.ofString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
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
                List<GraphQLRadioPlayShowGrouping> graphQLShows = Arrays.asList(gson.fromJson(programSetListString, GraphQLRadioPlayShowGrouping[].class));
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
                return graphQLShows;
            } else {
                throw new ImportException("Fehler beim Lesen der GraphQL " + response.statusCode());
            }
        } catch (IOException exception){
            throw new ImportException(exception.getMessage(), exception);
        }
    }

    /**
     *
     * @return
     * @throws InterruptedException
     * @throws ImportException
     */
    List<GraphQLPublisherGrouping> getPublisherFromGraphQL() throws InterruptedException, ImportException {

        LOG.info("Lese Publisher aus GraphQL...");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        HttpRequest.BodyPublisher body;
        try {
            InputStream inputStream = loader.getResourceAsStream("graphqlrequest_publisher.json");
            body = HttpRequest.BodyPublishers.ofString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
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
                String publisherListString = gson.toJson(
                        ((Map<String,Object>)((Map<String, Object>)responseMap
                                .get("data"))
                                .get("publicationServices"))
                                .get("nodes")
                );
                return Arrays.asList(gson.fromJson(publisherListString, GraphQLPublisherGrouping[].class));
            } else {
                throw new ImportException("Fehler beim Lesen der GraphQL! " + response.statusCode());
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
