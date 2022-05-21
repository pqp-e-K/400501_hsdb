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

    String userPass = "deliver:J9Xsbzg4SkHvjvrgpx*c";
    String authorizationString = "Basic " + Base64.encodeBase64String(userPass.getBytes(StandardCharsets.UTF_8));

    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();
    private static final DataExtractor DATA_EXTRACTOR = new DataExtractor();
    static final String[] AUDIOTHEK_EXCLUDES = CONFIG.getProperty(Config.AUDIOTHEK_EXCLUDES,"").split(",");

    public AudiothekDaoV2(){}

    /**
     *
     * @return
     * @throws ImportException
     */
    public Map<String, GenericObject> getRadioPlays() throws ImportException, InterruptedException {
        // get all shows
        List<GraphQLGrouping> graphQLShows = getRadioPlayShowsFromGraphQL();

        // Remove "Lesungen" id: 7258744
        LOG.info("Entferne Lesungen...");
        graphQLShows = graphQLShows.stream().filter(show -> !Objects.equals(show.getId(), "7258744")).collect(Collectors.toList());

        String v2ApiRequestUrl = API_URL + "/items/episodes/";

        Map<String, GenericObject> radioPlays = new HashMap<>();

        graphQLShows.forEach(
                show -> Arrays.stream(show.getItems().getEdges()).sequential().forEach(
                        edge -> {
                            try {
                                String externalId = edge.getNode().getEpisodeId();
                                V2ApiEpisode episode = null;
                                if(null != externalId) {
                                    episode = getEpisodeFromCoreV2Api(getRawResultFromCoreV2Api(
                                                    v2ApiRequestUrl + externalId,
                                                    true
                                            )
                                    );
                                    cacheEpisode(episode);
                                }
                                radioPlays.put(edge.getNode().getId(), radioPlayFromApiResults(episode, edge.getNode()));
                            } catch (ImportException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                )
        );

        return radioPlays;
    }

    /**
     *
     * @param v2ApiEpisode
     * @param graphQLNode
     * @return
     */
    GenericObject radioPlayFromApiResults(V2ApiEpisode v2ApiEpisode, GraphQLNode graphQLNode){
        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,graphQLNode.getId());

        HashSet<String> titles = new HashSet<>();
        String title = graphQLNode.getTitle();
        titles.add(title);
        if(null != v2ApiEpisode){
            title = v2ApiEpisode.getSelf().getTitle();
            titles.add(title);
        }
        radioPlay.addDescriptionProperty(RadioPlayType.TITLE, new ArrayList<>(titles));
        radioPlay.addDescriptionProperty(RadioPlayType.LINK, graphQLNode.getSharingUrl());

        //Episodennummer aus Titel lesen
        Integer episode = DataExtractor.getEpisodeFromTitle(title);
        if(episode != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.EPISODE, String.valueOf(episode));
        }

        //Staffelnummer aus Titel lesen
        Integer season = DataExtractor.getSeasonFromTitle(title);
        if(season != null) {
            radioPlay.addDescriptionProperty(RadioPlayType.SEASON, String.valueOf(season));
        }

        if( null != graphQLNode.getGraphQLDocument()) {
            Integer duration = graphQLNode.getGraphQLDocument().getDuration();
            radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(duration));
        }

        if( null != v2ApiEpisode ) {
            try {
                if(null != v2ApiEpisode.getPremiereDate()) {
                    radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(v2ApiEpisode.getPremiereDate()));
                }
            } catch (DataHarmonizerException e) {
                LOG.warn(e.getMessage(), e);
            }
            radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(v2ApiEpisode.getDuration()));
            radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, v2ApiEpisode.getProducer());
            if( null != v2ApiEpisode.getDescription() ) {
                radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, v2ApiEpisode.getDescription());
            }
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_ID, v2ApiEpisode.getParentAsset().getId());
            radioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE, v2ApiEpisode.getParentAsset().getTitle());
        }

        return radioPlay;
    }

    /**
     *
     * @return
     * @throws ImportException
     */
    List<V2ApiPage> getAllShowAssetPages() throws ImportException {
        return getAllPagesForRessource(API_URL+"/groupings/shows?size="+LIMIT, false);
    }

    /**
     *
     * @param pageHref
     * @param decompress
     * @return
     * @throws ImportException
     */
    List<V2ApiPage> getAllPagesForRessource(String pageHref, boolean decompress) throws ImportException {
        List<V2ApiPage> pages = new ArrayList<>();
        paginateCoreV2Api(pages, pageHref, decompress);
        return pages;
    }

    /**
     *
     * @param resultList
     * @param href
     * @param decompress
     * @throws ImportException
     */
    void paginateCoreV2Api(List<V2ApiPage> resultList, String href, boolean decompress) throws ImportException {
        V2ApiPage page = getPageFromCoreV2Api(getRawResultFromCoreV2Api(href,decompress));
        resultList.add(page);
        if( page.hasNext() ){
            paginateCoreV2Api(resultList, page.getNext().getHref(),decompress);
        }
    }

    /**
     *
     * @param rawPage
     * @return
     */
    V2ApiEpisode getEpisodeFromCoreV2Api(String rawPage){
        return gson.fromJson(rawPage, V2ApiEpisode.class);
    }

    /**
     *
     * @param rawPage
     * @return
     */
    V2ApiPage getPageFromCoreV2Api(String rawPage){
        return gson.fromJson(rawPage, V2ApiPage.class);
    }

    /**
     *
     * @param pageHref
     * @param decompress
     * @return
     * @throws ImportException
     */
    String getRawResultFromCoreV2Api(String pageHref, boolean decompress) throws ImportException {
        LOG.info("GET "+pageHref);
        String[] parts = pageHref.split("/");
        String id = parts[parts.length-1];

        // id in cache?
        String path = "api-cache/"+id.replace(":","_")+".json";
        File file = new File(path);
        if(file.exists()){
            LOG.info("Loading {} from cache!", id);
            try {
                return Files.readString(Path.of(path));
            } catch (IOException e) {
                LOG.error("Failed to load {} from cache!", id);
                LOG.error(e.getMessage(),e);
            }
        }

        HttpClient httpClient;
        if(null != PROXY_HOST) {
            LOG.info("Proxy set to {}", PROXY_HOST);
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(20))
                    .proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT))))
                    .build();
        } else {
            httpClient = HttpClient.newHttpClient();
        }

        if( httpClient.proxy().isPresent() ){
            LOG.info("Using proxy...");
        }

        HttpRequest request =  HttpRequest.newBuilder()
                .uri(URI.create(pageHref))
                .header(HttpHeaders.CONTENT_TYPE,HEADER_ACCEPT_CONTENT_TYPE)
                .header(HttpHeaders.ACCEPT,HEADER_ACCEPT_CONTENT_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                .header(HttpHeaders.AUTHORIZATION, authorizationString)
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                byte[] encodedResponse;
                if(decompress) {
                    encodedResponse = gzipDecompress(response.body());
                } else {
                    encodedResponse = response.body();
                }
                return new String(encodedResponse, StandardCharsets.UTF_8);
            } else {
                throw new ImportException("Response: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImportException(e.getMessage(), e);
        }
    }


    /**
     *
     * @return
     * @throws ImportException
     * @throws IOException
     */
    String fetchFromCoreV2Api() throws ImportException, InterruptedException {
        return this.fetchFromCoreV2Api(API_URL, RESOURCE, false);
    }

    /**
     *
     * @param apiUrl
     * @param resource
     * @param decompress
     * @return
     * @throws IOException
     * @throws ImportException
     */
    String fetchFromCoreV2Api(String apiUrl, String resource, boolean decompress) throws ImportException, InterruptedException {
        LOG.info("GET {}", resource);
        HttpClient httpClient;
        if(null != PROXY_HOST) {
            LOG.info("Proxy set to {}", PROXY_HOST);
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(20))
                    .proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT))))
                    .build();
        } else {
            httpClient = HttpClient.newHttpClient();
        }

        if( httpClient.proxy().isPresent() ){
            LOG.info("Using proxy...");
        }

        HttpRequest request =  HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/" + resource))
                .header(HttpHeaders.CONTENT_TYPE,HEADER_ACCEPT_CONTENT_TYPE)
                .header(HttpHeaders.ACCEPT,HEADER_ACCEPT_CONTENT_TYPE)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                .header(HttpHeaders.AUTHORIZATION, authorizationString)
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                byte[] encodedResponse;
                if(decompress) {
                    encodedResponse = gzipDecompress(response.body());
                } else {
                    encodedResponse = response.body();
                }
                return new String(encodedResponse, StandardCharsets.UTF_8);
            } else {
                throw new ImportException("Response: " + response.statusCode());
            }
        } catch (IOException e ) {
            throw new ImportException(e.getMessage(), e);
        }
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
                    Path.of(Objects.requireNonNull(loader.getResource("graphqlrequest.json")).getFile()));
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

    void cacheEpisode(V2ApiEpisode episode){
        if( null != episode ){
            try {
                FileWriter writer = new FileWriter("api-cache/"+episode.getSelf().getId().replace(":","_")+".json");
                gson.toJson(episode,writer);
                writer.flush();
            } catch (IOException e) {
                LOG.error(e.getMessage(),e);
            }
        }
    }
}
