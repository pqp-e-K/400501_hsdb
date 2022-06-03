package systems.pqp.hsdb.dao.graphql;

import java.util.Arrays;

public class GraphQLDocument {

    private String id;
    private String externalId;
    private Long coremediaId;
    private String title;
    private String description;
    private String longDescription;
    private String producer;
    private String startDate;
    private Integer duration;
    private Boolean isPublished;
    private Integer episodeNumber;
    private String publisherId;
    private GraphQLFusedBy[] fusedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getCoremediaId() {
        return coremediaId;
    }

    public void setCoremediaId(Long coremediaId) {
        this.coremediaId = coremediaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public GraphQLFusedBy[] getFusedBy() {
        return fusedBy;
    }

    public void setFusedBy(GraphQLFusedBy[] fusedBy) {
        this.fusedBy = fusedBy;
    }

    @Override
    public String toString() {
        return "GraphQLDocument{" +
                "id='" + id + '\'' +
                ", externalId='" + externalId + '\'' +
                ", coremediaId=" + coremediaId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", producer='" + producer + '\'' +
                ", startDate='" + startDate + '\'' +
                ", duration=" + duration +
                ", isPublished=" + isPublished +
                ", episodeNumber=" + episodeNumber +
                ", publisherId='" + publisherId + '\'' +
                ", fusedBy=" + Arrays.toString(fusedBy) +
                '}';
    }
}
