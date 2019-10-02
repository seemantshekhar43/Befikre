package com.seemantshekhar.befikre.Model;

public class User {
    private String user_id;
    private String username;
    private String full_name;
    private String profile_image;

    public User(String user_id, String username, String fullName, String profile_image) {
        this.user_id = user_id;
        this.username = username;
        this.full_name = fullName;
        this.profile_image = profile_image;
    }

    public User(){

    }

    public String getId() {
        return user_id;
    }

    public void setId(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String fullName) {
        this.full_name = fullName;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\n' +
                "username='" + username + '\n' +
                "full_name" + full_name + '\n' +
                "profile_image='" + profile_image + '\n' +
                '}';
    }
}
