package com.seemantshekhar.befikre.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {
    private String text;
    private String comment_id;
    private String publisher;
    private String date_created;

    public Comment(){

    }
    public Comment(String text, String comment_id, String publisher, String date_created) {
        this.text = text;
        this.comment_id = comment_id;
        this.publisher = publisher;
        this.date_created = date_created;
    }

    protected Comment(Parcel in) {
        text = in.readString();
        comment_id = in.readString();
        publisher = in.readString();
        date_created = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", comment_id='" + comment_id + '\'' +
                ", publisher='" + publisher + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(comment_id);
        dest.writeString(publisher);
        dest.writeString(date_created);
    }
}
