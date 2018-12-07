package project.beryl.com.newfirebaseapplication.model;

public class UserModel {
    private String name;
    private String userId;
    private String email;
    private String phone;
    private String photoUrl;

    public UserModel(){

    }

    public UserModel(String name,String userId,String email,String phone,String photoUrl){
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.photoUrl = photoUrl;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
