package systems.pqp.hsdb.dao.coreapi;

public class V2ApiEpisode {

    private V2ApiLink self;
    private String externalId;
    private String created;
    private String modified;
    private String description;
    private String longDescription;
    private Object duration;
    private String producer;
    private V2ApiLink parentAsset;
    private String premiereDate;
    private Integer episodeNumber;

    public V2ApiLink getSelf() {
        return self;
    }

    public void setSelf(V2ApiLink self) {
        this.self = self;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
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

    public Object getDuration() {
        return duration;
    }

    public void setDuration(Object duration) {
        this.duration = duration;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public V2ApiLink getParentAsset() {
        return parentAsset;
    }

    public void setParentAsset(V2ApiLink parentAsset) {
        this.parentAsset = parentAsset;
    }

    public String getPremiereDate() {
        return premiereDate;
    }

    public void setPremiereDate(String premiereDate) {
        this.premiereDate = premiereDate;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }
}
