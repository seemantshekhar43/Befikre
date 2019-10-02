package com.seemantshekhar.befikre.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String postID;
    private String publisher;
    private String description;
    private String postImage;
    private String dateCreated;

    public Post() {
    }

    public Post(String postID, String publisher, String description, String postImage, String dateCreated) {
        this.postID = postID;
        this.publisher = publisher;
        this.description = description;
        this.postImage = postImage;
        this.dateCreated = dateCreated;
    }

    protected Post(Parcel in) {
        postID = in.readString();
        publisher = in.readString();
        description = in.readString();
        postImage = in.readString();
        dateCreated = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postID='" + postID + '\n' +
                ", publisher='" + publisher + '\n' +
                ", description='" + description + '\n' +
                ", postImage='" + postImage + '\n' +
                ", dateCreated='" + dateCreated + '\n' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postID);
        dest.writeString(publisher);
        dest.writeString(description);
        dest.writeString(postImage);
        dest.writeString(dateCreated);
    }
}
