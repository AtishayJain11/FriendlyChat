package project.beryl.com.newfirebaseapplication.model;

/**
 * reated by punamchand 12/12/2018alam on 10/29/2017.
 */

public class ChatModel {
    private String lastMessage;
    private String seen;
    private String type;
    private String time;
    private String otherUserId;
    private String image;
    private String name;
    private long count;
    private String from;
    private String typing;

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public ChatModel(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ChatModel(String otherUserId, String lastMessage, String seen, String type, String time, String image, String name, long count, String from, String typing) {
        this.otherUserId = otherUserId;
        this.lastMessage = lastMessage;
        this.seen = seen;
        this.type = type;
        this.time = time;
        this.image = image;
        this.name = name;
        this.count = count;
        this.from = from;
        this.typing = typing;

    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
