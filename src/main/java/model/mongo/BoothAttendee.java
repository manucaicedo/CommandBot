package model.mongo;

public class BoothAttendee {

    private String attendeeEmail;
    private String booth;

    public BoothAttendee(String attendeeEmail, String booth) {
        this.attendeeEmail = attendeeEmail;
        this.booth = booth;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }

    public String getBooth() {
        return booth;
    }

    public void setBooth(String booth) {
        this.booth = booth;
    }
}
