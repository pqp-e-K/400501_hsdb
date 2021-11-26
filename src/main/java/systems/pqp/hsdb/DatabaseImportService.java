package systems.pqp.hsdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseImportService {

    private static final String USER = "root";
    private static final String PASS = "root";
    private static final String URL  = "localhost:3306";
    private static final String DB   = "hsdb";
    private static final String TAB  = "hs_du";
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseImportService.class.getName());

    public DatabaseImportService() {}

    /**
     * Connect to mariadb
     * @return Connection
     * @throws SQLException if connection fails
     */
    Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mariadb://"+URL+"/"+DB+"?user="+USER+"&password="+PASS+"");
    }

    public List<GenericObject> getRadioPlays(){
        return getRadioPlays("");
    }

    public List<GenericObject> getRadioPlays(String query){
        List<GenericObject> result = new ArrayList<>();
        String sql = "SELECT DUKEY, VOLLINFO FROM "+DB+"."+TAB+" "+query+";";
        try(Connection connection = createConnection()) {
            try(Statement stmt = connection.createStatement()){
                ResultSet resultSet = stmt.executeQuery(sql);
                while(resultSet.next()){
                    String id = resultSet.getString(1);
                    String xml = resultSet.getString(2);

                    GenericObject radioPlay = genericObjectFromBean(
                            id,
                            beanFromXmlString(xml)
                    );
                    result.add(radioPlay);
                }
            } catch (SQLException | JsonProcessingException throwables){
                LOG.error(throwables.getMessage(), throwables);
            }
        } catch (SQLException throwables) {
            LOG.error(throwables.getMessage(), throwables);
        }

        return result;
    }

    /***
     *
     * @param id
     * @param bean
     * @return
     */
    GenericObject genericObjectFromBean(String id, VollinfoBean bean){
        GenericModel genericModel = new GenericModel(RadioPlayType.class);
        GenericObject radioPlay = new GenericObject(genericModel,id);

        try {
            radioPlay.addDescriptionProperty(RadioPlayType.TITLE, bean.getTitle());
            radioPlay.addDescriptionProperty(RadioPlayType.BIO, bean.getBio());
            radioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(bean.getDurationInSeconds()));
            radioPlay.addDescriptionProperty(RadioPlayType.PUBLICATION_DT, bean.getPublicationDt());
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
     *
     * @param xml
     * @return
     * @throws JsonProcessingException
     */
    VollinfoBean beanFromXmlString(String xml) throws JsonProcessingException {

        return XML_MAPPER.readValue(xml, VollinfoBean.class);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class VollinfoBean {

        @JsonProperty("KAT")
        private String category = "";
        @JsonProperty("AUT")
        private String author = "";
        @JsonProperty("UEB")
        private String translator = "";
        @JsonProperty("RHTI")
        private String title = "";
        @JsonProperty("LITV")
        private String longTitle = "";
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
                seconds += Float.parseFloat(parts[0]);
                if( parts.length > 1 ){ // 5'21 -> seconds = 5 * 60 + 21
                    seconds += Float.parseFloat(parts[1]) * 60;
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
            return getActors().stream().map(ActorBean::getName).collect(Collectors.toList());
        }

        public List<String> getActorRoles() {
            return getActors().stream().map(ActorBean::getRolle).collect(Collectors.toList());
        }

        public List<String> getInvolvedNames() {
            return Stream.concat(
                    Stream.of(getAuthor(), getDirector(), getTranslator()),
                    getActorNames().stream()
            ).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "VollinfoBean{" +
                    "category='" + category + '\'' +
                    ", author='" + author + '\'' +
                    ", translator='" + translator + '\'' +
                    ", title='" + title + '\'' +
                    ", longTitle='" + longTitle + '\'' +
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
    static class ActorBean {
        @JacksonXmlProperty(isAttribute = true)
        private String rolle = "";
        @JsonProperty("NAM")
        @JacksonXmlText
        private String name = "";

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


}
