package project.beryl.com.newfirebaseapplication.model;

/**
 * reated by punamchand 12/12/2018alam on 10/29/2017.
 */

public class MessageModel {
    private String message_text;
    private  String seen;
    private  String time;
    private  String type;
    private String from;
    public MessageModel() {
    }

    public MessageModel(String messageTexts, String seen, String time, String type, String from) {
        this.message_text = messageTexts;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;
    }

    public String getMessage_text() {
        return message_text;
    }

    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
