package model.mongo;

import org.bson.types.ObjectId;

public class SpeakerSpelling {

    private String speakerId;
    private String spelling;

    public String getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public String getSpelling() {
        return spelling;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }
}
