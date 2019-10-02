package com.seemantshekhar.befikre.Model;

public class Reply {

    private String text;
    private String reply_id;
    private String publisher;
    private String date_created;

    public Reply(String text, String reply_id, String publisher, String date_created) {
        this.text = text;
        this.reply_id = reply_id;
        this.publisher = publisher;
        this.date_created = date_created;
    }

    public Reply(){

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
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
        return "Reply{" +
                "text='" + text + '\'' +
                ", reply_id='" + reply_id + '\'' +
                ", publisher='" + publisher + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
