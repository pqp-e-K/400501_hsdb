package systems.pqp.hsdb;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 */
public class Similarity implements Serializable {

    private String id;
    private String dukey;
    private String audiothekId;
    private Float score = 0.0F;
    private String audiothekLink;
    private LocalDateTime validationDateTime;
    private boolean deleted;

    public Similarity(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDukey() {
        return dukey;
    }

    public void setDukey(String dukey) {
        this.dukey = dukey;
    }

    public String getAudiothekId() {
        return audiothekId;
    }

    public void setAudiothekId(String audiothekId) {
        this.audiothekId = audiothekId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getAudiothekLink() {
        return audiothekLink;
    }

    public void setAudiothekLink(String audiothekLink) {
        this.audiothekLink = audiothekLink;
    }

    public LocalDateTime getValidationDateTime() {
        return validationDateTime;
    }

    public void setValidationDateTime(LocalDateTime validationDateTime) {
        this.validationDateTime = validationDateTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Similarity{" +
                "id='" + id + '\'' +
                ", dukey='" + dukey + '\'' +
                ", audiothekId='" + audiothekId + '\'' +
                ", audiothekLink='" + audiothekLink + '\'' +
                ", score='" + score + '\'' +
                ", validationDateTime=" + validationDateTime +
                ", deleted=" + deleted +
                '}';
    }
}
