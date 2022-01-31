package systems.pqp.hsdb.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.Config;
import systems.pqp.hsdb.DataHarmonizer;
import systems.pqp.hsdb.DataHarmonizerException;
import systems.pqp.hsdb.SimilarityBean;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Data-Access-Object für HSDB-Datenbank
 */
public class HsdbDao {

    private static final Logger LOG = LogManager.getLogger(HsdbDao.class.getName());
    private static final Config CONFIG = Config.Config();

    private static final String USER = CONFIG.getProperty(Config.HSDB_USER);
    private static final String PASS = CONFIG.getProperty(Config.HSDB_PASS);
    private static final String URL  = CONFIG.getProperty(Config.HSDB_URL);
    private static final String DB   = CONFIG.getProperty(Config.HSDB_DB);
    private static final String TAB  = CONFIG.getProperty(Config.HSDB_TABLE);
    private static final String MAPPING_TABLE_COL_ID = "ID";
    private static final String MAPPING_TABLE_COL_DUKEY = "DUKEY";
    private static final String MAPPING_TABLE_COL_AUDIOTHEK_ID = "AUDIOTHEK_ID";
    private static final String MAPPING_TABLE_COL_DELETED = "DELETED";
    private static final String MAPPING_TABLE_COL_AUDIOTHEK_LINK = "AUDIOTHEK_LINK";
    private static final String MAPPING_TABLE_COL_VALIDATION_DATE = "VALIDATION_DT";
    private static final String MAPPING_TAB = CONFIG.getProperty(Config.HSDB_MAPPING_TABLE);
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private static final DataHarmonizer DATA_HARMONIZER = new DataHarmonizer();

    private static final String UPSERT_CHECK_QUERY = String.format(
            "SELECT %s FROM %s.%s WHERE %s = ? AND %s = ?",
            MAPPING_TABLE_COL_ID,
            DB,
            MAPPING_TAB,
            MAPPING_TABLE_COL_DUKEY,
            MAPPING_TABLE_COL_AUDIOTHEK_ID
    );
    private static final String UPDATE_STMT = String.format(
            "UPDATE %s.%s SET %s = ?, %s = ? WHERE %s = ?",
            DB,
            MAPPING_TAB,
            MAPPING_TABLE_COL_AUDIOTHEK_LINK,
            MAPPING_TABLE_COL_VALIDATION_DATE,
            MAPPING_TABLE_COL_ID
    );
    private static final String INSERT_STMT = String.format(
            "INSERT INTO %s.%s(%s,%s,%s,%s,%s) VALUES(?,?,?,?,?)",
            DB,
            MAPPING_TAB,
            MAPPING_TABLE_COL_DUKEY,
            MAPPING_TABLE_COL_AUDIOTHEK_ID,
            MAPPING_TABLE_COL_AUDIOTHEK_LINK,
            MAPPING_TABLE_COL_VALIDATION_DATE,
            MAPPING_TABLE_COL_DELETED
    );

    public HsdbDao() {}

    /**
     * Upsert eine List aus SimilarityBean-Objekten
     * @param similarities
     */
    public void upsertMany(List<SimilarityBean> similarities){
        try(
                Connection connection = createConnection();
                PreparedStatement upsertCheck = connection.prepareStatement(UPSERT_CHECK_QUERY);
                PreparedStatement update = connection.prepareStatement(UPDATE_STMT);
                PreparedStatement insert = connection.prepareStatement(INSERT_STMT);
        ){
            connection.setAutoCommit(false);
            similarities.forEach(
                    similarityBean -> {
                        try {
                            if(checkUpsert(upsertCheck, similarityBean)){
                                // update
                                updateOne(update, similarityBean, true);
                            } else {
                                // insert
                                insertOne(insert, similarityBean, true);
                            }
                        } catch (SQLException e) {
                            LOG.error("Upsert similiarity failed for {}", similarityBean, e);
                        }
                    }
            );
            connection.commit();
        } catch (SQLException throwables) {
            LOG.error(throwables.getMessage(), throwables);
        }
    }

    /**
     *
     * @param upsertCheck
     * @param bean
     * @return
     * @throws SQLException
     */
    private boolean checkUpsert(PreparedStatement upsertCheck, SimilarityBean bean) throws SQLException {
        upsertCheck.setString(1, bean.getDukey());
        upsertCheck.setString(2, bean.getAudiothekId());
        ResultSet checkResult = upsertCheck.executeQuery();
        if( checkResult.getFetchSize() > 1 ){ // kann eigentlich nie passieren
            LOG.warn("Mehrere Einträge in {}.{} gefunden für {},{}",DB,MAPPING_TAB,bean.getDukey(),bean.getAudiothekId());
        }

        if( checkResult.next() ){
            bean.setId(checkResult.getString(1));
            return true;
        }
        return false;
    }

    /**
     *
     * @param update
     * @param similarityBean
     * @return
     * @throws SQLException
     */
    public String updateOne(PreparedStatement update, SimilarityBean similarityBean, boolean execute) throws SQLException {
        update.setString(1, similarityBean.getAudiothekLink());
        update.setString(2, similarityBean.getValidationDateTime().toString());
        update.setString(3, similarityBean.getId());
        if(execute){
            update.executeUpdate();
        }
        return similarityBean.getId();
    }

    /**
     *
     * @param insert
     * @param similarityBean
     * @param execute
     * @throws SQLException
     */
    public void insertOne(PreparedStatement insert, SimilarityBean similarityBean, boolean execute) throws SQLException {
        insert.setString(1, similarityBean.getDukey());
        insert.setString(2, similarityBean.getAudiothekId());
        insert.setString(3, similarityBean.getAudiothekLink());
        insert.setString(4, similarityBean.getValidationDateTime().toString());
        insert.setBoolean(5, false);
        if(execute) {
            insert.executeUpdate();
        }
    }

    /**
     * Gibt Map mit Key=DUKEY(String) und Value=GenericObject
     * @return Map<String,GenericObject>
     */
    public Map<String,GenericObject> getRadioPlays(){
        return getRadioPlays("");
    }

    /**
     * Gibt Map mit Key=DUKEY(String) und Value=GenericObject
     * @param query SQL-Query-String, z.B. "WHERE id=123 AND foo > 0"
     * @return Map<String,GenericObject>
     */
    public Map<String,GenericObject> getRadioPlays(String query){
        Map<String,GenericObject> result = new HashMap<>();
        String sql = "SELECT DUKEY, VOLLINFO, SORTRFA FROM "+DB+"."+TAB+" "+query+";";
        try(Connection connection = createConnection()) {
            try(Statement stmt = connection.createStatement()){
                ResultSet resultSet = stmt.executeQuery(sql);
                while(resultSet.next()){
                    String id = resultSet.getString(1);
                    String xml = resultSet.getString(2);
                    String publisher = resultSet.getString(3);

                    VollinfoBean bean = beanFromXmlString(xml);

                    if( LOG.isDebugEnabled() ){
                        LOG.debug(bean.toString());
                    }

                    GenericObject radioPlay = genericObjectFromBean(
                            id,
                            bean
                    );
                    radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, publisher);
                    result.put(id,radioPlay);
                }
            } catch (SQLException | JsonProcessingException throwables){
                LOG.error(throwables.getMessage(), throwables);
            }
        } catch (SQLException throwables) {
            LOG.error(throwables.getMessage(), throwables);
        }

        return result;
    }

    /**
     * Parsed VollinfoBean zu GenericObject
     * @param id String, DUKEY
     * @param bean VollinfoBean
     * @return GenericObject
     */
    GenericObject genericObjectFromBean(String id, VollinfoBean bean){
        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,id);

        try {
            List<String> titles = new ArrayList<>(List.of(bean.getTitle()));
            if( !"".equals(bean.getSubTitle() )){
                titles.add(bean.getSubTitle());
            }
            radioPlay.addDescriptionProperty(RadioPlayType.TITLE, titles);
            radioPlay.addDescriptionProperty(RadioPlayType.SHOW_TITLE, bean.getShowTitle());
            radioPlay.addDescriptionProperty(RadioPlayType.BIO, bean.getBio());
            radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(bean.getDurationInSeconds()));
            try {
                radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, DATA_HARMONIZER.date(bean.getPublicationDt()));
            } catch (DataHarmonizerException e) {
                LOG.warn(e.getMessage(), e);
            }
            radioPlay.addDescriptionProperty(RadioPlayType.BIO, bean.getBio());
            radioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION, bean.getDescription());
            radioPlay.addDescriptionProperty(RadioPlayType.LONG_TITLE, bean.getLongTitle());
            radioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER, null == bean.getProductionCompany() ? "" : bean.getProductionCompany());
            radioPlay.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, bean.getInvolvedNames());
            radioPlay.addDescriptionProperty(RadioPlayType.PERSON_ROLE, bean.getActorRoles());

        } catch (IllegalArgumentException exception){
            LOG.error(exception.getMessage(), exception);
            LOG.info(bean.toString());
        }
        return radioPlay;
    }

    /**
     * Parsed XML-String zu VollinfoBean-Objekt
     * @param xml String
     * @return VollinfoBean
     * @throws JsonProcessingException
     */
    VollinfoBean beanFromXmlString(String xml) throws JsonProcessingException {

        return XML_MAPPER.readValue(xml, VollinfoBean.class);

    }

    /**
     * Java-Bean für VOLLINFO-XML-Daten in hs_du-Tabelle
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class VollinfoBean implements Serializable {

        @JsonProperty("KAT")
        private String category = "";
        @JsonProperty("AUT")
        private String author = "";
        @JsonProperty("UEB")
        private String translator = "";
        @JsonProperty("RTI")
        private String showTitle="";
        @JsonProperty("RHTI")
        private String title = "";
        @JsonProperty("LITV")
        private String longTitle = "";
        @JsonProperty("UNTI")
        private String subTitle = "";
        @JsonProperty("BIO")
        private String bio = "";
        @JsonProperty("INH")
        private String description = "";
        @JsonProperty("SPR")
        private List<ActorBean> actors = new ArrayList<>();
        @JsonProperty("REG")
        private String director = "";
        @JsonProperty("ESD")
        private String publicationDt = "";
        @JsonProperty("DAU")
        private String duration = "";
        @JsonProperty("PROD")
        private String productionCompany = "";

        public VollinfoBean(){}

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTranslator() {
            return translator;
        }

        public void setTranslator(String translator) {
            this.translator = translator;
        }

        public String getShowTitle() {
            return showTitle;
        }

        public void setShowTitle(String showTitle) {
            this.showTitle = showTitle;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLongTitle() {
            return longTitle;
        }

        public void setLongTitle(String longTitle) {
            this.longTitle = longTitle;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ActorBean> getActors() {
            return actors;
        }

        public void setActors(List<ActorBean> actors) {
            this.actors = actors;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getPublicationDt() {
            return publicationDt;
        }

        public void setPublicationDt(String publicationDt) {
            this.publicationDt = publicationDt;
        }

        public String getDuration() {
            return duration;
        }

        public Float getDurationInSeconds() {
            if("".equals(duration)){
                return 0F;
            }

            // extract numeric values from duration-string, e.g. "ca. 160" -> 160
            String regex = "([0-9']+)";
            Pattern durationPattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
            Matcher tagMatcher = durationPattern.matcher(duration);
            Set<String> result = tagMatcher.results().map(
                    MatchResult::group
            ).collect(Collectors.toSet());
            if(result.isEmpty()){
                return 0F;
            }
            String cleanDuration = result.iterator().next();

            String[] parts = cleanDuration.split("'"); // 42'18

            float seconds = 0F;
            if( parts.length > 0 ){
                seconds += Float.parseFloat(parts[0]) * 60;
                if( parts.length > 1 ){ // 5'21 -> seconds = 5 * 60 + 21
                    seconds += Float.parseFloat(parts[1]);
                }
            }
            return seconds;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getProductionCompany() {
            return productionCompany;
        }

        public void setProductionCompany(String productionCompany) {
            this.productionCompany = productionCompany;
        }

        public List<String> getActorNames() {
            return getActors().stream().map(ActorBean::getName).filter(name -> !"".equals(name)).collect(Collectors.toList());
        }

        public List<String> getActorRoles() {
            return getActors().stream().map(ActorBean::getRolle).filter(name -> !"".equals(name)).collect(Collectors.toList());
        }

        public List<String> getInvolvedNames() {

            List<String> involvedPersons = new ArrayList<>();
            if(!"".equals(getAuthor())){
                involvedPersons.add(getAuthor());
            }
            if(!"".equals(getDirector())){
                involvedPersons.add(getDirector());
            }
            if(!"".equals(getTranslator())){
                involvedPersons.add(getTranslator());
            }
            if(!getActorNames().isEmpty()){
                getActorNames().forEach(
                        actorName -> {
                            if(!"".equals(actorName)){
                                involvedPersons.add(actorName);
                            }
                        }
                );
            }

            return involvedPersons.stream().map(name -> name.replaceAll("\\[.*\\]", "").trim()).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "VollinfoBean{" +
                    "category='" + category + '\'' +
                    ", author='" + author + '\'' +
                    ", translator='" + translator + '\'' +
                    ", showTitle='" + showTitle + '\'' +
                    ", title='" + title + '\'' +
                    ", longTitle='" + longTitle + '\'' +
                    ", subTitle='" + subTitle + '\'' +
                    ", bio='" + bio + '\'' +
                    ", description='" + description + '\'' +
                    ", actors=" + actors +
                    ", director='" + director + '\'' +
                    ", publicationDt='" + publicationDt + '\'' +
                    ", duration='" + duration + '\'' +
                    ", productionCompany='" + productionCompany + '\'' +
                    '}';
        }
    }

    /**
     *
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ActorBean implements Serializable{
        @JacksonXmlProperty(isAttribute = true)
        private String rolle = "";
        @JsonProperty("NAM")
        @JacksonXmlText
        private String name = "";

        public ActorBean(){}

        public String getRolle() {
            return rolle;
        }

        public void setRolle(String rolle) {
            this.rolle = rolle;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Actor{" +
                    "rolle='" + rolle + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    /**
     * Connect to mariadb
     * @return Connection
     * @throws SQLException if connection fails
     */
    Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mariadb://"+URL+"/"+DB+"?user="+USER+"&password="+PASS+"");
    }

}
