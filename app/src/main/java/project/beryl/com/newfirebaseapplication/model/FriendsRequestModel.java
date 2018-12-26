package project.beryl.com.newfirebaseapplication.model;

/**
 * reated by punamchand 12/12/2018alam on 10/26/2017.
 */

public class FriendsRequestModel {
    public String user_id;
    public String name;
    public String status;
    public String request_type;
    public String thumb_image;
    public String request_date;

    public FriendsRequestModel() {
    }

    public FriendsRequestModel(String user_id, String name, String status, String request_type, String thumb_image, String requestDate ){
        this.user_id=user_id;
        this.name=name;
        this.status=status;
        this.request_type=request_type;
        this.thumb_image=thumb_image;
        this.request_date = requestDate;

    }
}
