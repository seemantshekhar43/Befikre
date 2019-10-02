package com.seemantshekhar.befikre.Model;


/*
    types of notification:
    1. "follow" - id: userID --- 'userID is now following you.'
    2. "like_post" - id: postID --- 'userID liked your post.'
    3. "like_comment" - id: postID + commentID --- 'userID liked your comment: commentText'
    4. "like_reply" - id: postID + commentID + replyID --- 'userID liked your reply: replyText'
    5. "comment" - id: postID + commentID --- 'userID commented on your post: commentText'
    6. "reply" - id: postID + commentID + replyID; --- 'userID replied to your comment: replyText'

 */

public class Notification {
    private String sender;
    private String receiver;
    private String type;
    private String text;
    private String time;
    private String post_id;
    private String notification_id;
    private String comment_id;
    private String reply_id;
    private String post_image;
    private boolean is_read;

    public Notification(){
    }

    public Notification(String sender, String receiver, String type, String text, String time, String post_id, String post_image, String notification_id) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.text = text;
        this.time = time;
        this.post_id = post_id;
        this.post_image = post_image;
        this.notification_id = notification_id;
        this.comment_id = "";
        this.reply_id = "";
        this.is_read = false;
    }

    public Notification(String sender, String receiver, String type, String text, String time, String post_id, String comment_id, String post_image , String notification_id) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.text = text;
        this.time = time;
        this.post_id = post_id;
        this.post_image = post_image;
        this.notification_id = notification_id;
        this.comment_id = comment_id;
        this.reply_id = "";
        this.is_read = false;
    }

    public Notification(String sender, String receiver, String type, String text, String time, String post_id, String comment_id, String reply_id, String post_image, String notification_id) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.text = text;
        this.time = time;
        this.post_id = post_id;
        this.post_image = post_image;
        this.notification_id = notification_id;
        this.comment_id = comment_id;
        this.reply_id = reply_id;
        this.is_read = false;
    }

    public Notification(String sender, String receiver, String type, String text, String time, String notification_id) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.text = text;
        this.time = time;
        this.post_id = "";
        this.post_image = "";
        this.notification_id = notification_id;
        this.comment_id = "";
        this.reply_id = "";
        this.is_read = false;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String id) {
        this.post_id = id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public boolean getIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", post_id='" + post_id + '\'' +
                ", notification_id='" + notification_id + '\'' +
                ", comment_id='" + comment_id + '\'' +
                ", reply_id='" + reply_id + '\'' +
                ", post_image='" + post_image + '\'' +
                '}';
    }
}
