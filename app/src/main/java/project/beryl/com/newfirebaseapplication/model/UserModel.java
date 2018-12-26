package project.beryl.com.newfirebaseapplication.model;

/**
 * reated by punamchand 12/12/2018alam on 10/17/2017.
 */

public class UserModel {
    public String user_id;
    public String name;
    public String status;
    public String image;
    public String thumb_image;

    public UserModel(){

    }
    public UserModel( String user_id, String name, String status,String image,String thumb_image ){
        this.user_id=user_id;
        this.name=name;
        this.status=status;
        this.image=image;
        this.thumb_image=thumb_image;

    }

}
