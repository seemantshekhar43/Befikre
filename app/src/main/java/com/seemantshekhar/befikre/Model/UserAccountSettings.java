package com.seemantshekhar.befikre.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAccountSettings implements Parcelable {
    private String bio;
    private String full_name;
    private long posts;
    private String username;
    private String profile_image;
    private String email;
    private String device_token;

    public UserAccountSettings (){

    }

    public UserAccountSettings(String bio, String full_name, long posts, String username, String profile_image, String email, String device_token) {
        this.bio = bio;
        this.full_name = full_name;
        this.posts = posts;
        this.username = username;
        this.profile_image = profile_image;
        this.email = email;
        this.device_token = device_token;
    }

    protected UserAccountSettings(Parcel in) {
        bio = in.readString();
        full_name = in.readString();
        posts = in.readLong();
        username = in.readString();
        profile_image = in.readString();
        email = in.readString();
        device_token = in.readString();
    }

    public static final Creator<UserAccountSettings> CREATOR = new Creator<UserAccountSettings>() {
        @Override
        public UserAccountSettings createFromParcel(Parcel in) {
            return new UserAccountSettings(in);
        }

        @Override
        public UserAccountSettings[] newArray(int size) {
            return new UserAccountSettings[size];
        }
    };

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "bio='" + bio + '\'' +
                ", full_name='" + full_name + '\'' +
                ", posts=" + posts +
                ", username='" + username + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bio);
        dest.writeString(full_name);
        dest.writeLong(posts);
        dest.writeString(username);
        dest.writeString(profile_image);
        dest.writeString(email);
        dest.writeString(device_token);
    }
}
