package com.seemantshekhar.befikre;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Model.Comment;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.Reply;
import com.seemantshekhar.befikre.Model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentNotificationActivity extends AppCompatActivity {
    private static final String TAG = "CommentNotificationActi";
    private static final String POST_ID = "postID";
    private static final String COMMENT_ID = "commentID";
    private static final String REPLY_ID = "replyID";
    private static final String PROFILE_ID = "profileID";
    public static final String POST = "post";
    private static final String ID = "id";
    private static final String TITLE = "title";

    private String postID = "";
    private String commentID = "";
    private String replyID = "";

    private ConstraintLayout commentLayout;
    private ConstraintLayout master;
    private ProgressBar progressBar;
    private ConstraintLayout replyLayout;
    private ImageButton backBtn;
    private NestedScrollView scrollView;

    //Post ui
    private CircleImageView imageProfilePost;
    private ImageView imagePost;
    private TextView usernamePost;
    private TextView timePost;
    private ImageButton menuPost;
    private TextView descPost;
    private TextView likeCountPost;
    private TextView commentCountPost;
    private ImageButton likeBtnPost;
    private ImageButton likePressedBtnPost;
    private ImageButton shareBtnPost;
    private ImageButton commentBtnPost;

    //Comment ui
    private CircleImageView imageProfileComment;
    private TextView usernameComment;
    private TextView timeComment;
    private TextView textComment;
    private TextView likeCountComment;
    private TextView replyCountComment;
    private ImageButton likeBtnComment;
    private ImageButton likePressedBtnComment;
    private ImageButton replyBtnComment;
    private TextView viewAllComments;

    //Reply ui
    private CircleImageView imageProfileReply;
    private TextView usernameReply;
    private TextView timeReply;
    private TextView textReply;
    private TextView likeCountReply;
    private ImageButton likeBtnReply;
    private ImageButton likePressedBtnReply;
    private TextView replyTO;


    private Post post;
    private Comment comment;
    private Reply reply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_notification);

        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().getString(POST_ID) != null){
                postID = getIntent().getExtras().getString(POST_ID);
            }
            if(getIntent().getExtras().getString(COMMENT_ID) != null){
                commentID = getIntent().getExtras().getString(COMMENT_ID);
            }
            if(getIntent().getExtras().getString(REPLY_ID) != null){
                replyID = getIntent().getExtras().getString(REPLY_ID);
            }

        }

        commentLayout = (ConstraintLayout) findViewById(R.id.comment_layout_comment_notification);
        master = (ConstraintLayout) findViewById(R.id.master);
        replyLayout = (ConstraintLayout) findViewById(R.id.reply_layout_comment_notification);
        backBtn = (ImageButton) findViewById(R.id.back_btn_comment_notification);
        scrollView = (NestedScrollView) findViewById(R.id.scroll);
        progressBar = (ProgressBar) findViewById(R.id.progress_circular_comment_notification);


        //Post ui
        imageProfilePost = (CircleImageView) findViewById(R.id.image_profile_comment_notification);
        imagePost = (ImageView) findViewById(R.id.post_image_comment_notification);
        usernamePost = (TextView) findViewById(R.id.username_comment_notification);
        timePost = (TextView) findViewById(R.id.time_comment_notification);
        menuPost = (ImageButton) findViewById(R.id.menu_comment_notification);
        descPost = (TextView) findViewById(R.id.post_desc_comment_notification);
        likeCountPost = (TextView) findViewById(R.id.post_like_count_comment_notification);
        commentCountPost = (TextView) findViewById(R.id.post_comment_count_comment_notification);
        likeBtnPost = (ImageButton) findViewById(R.id.like_btn_comment_notification);
        likePressedBtnPost = (ImageButton) findViewById(R.id.like_btn_pressed_comment_notification);
        shareBtnPost = (ImageButton) findViewById(R.id.share_btn_comment_notification);
        commentBtnPost = (ImageButton) findViewById(R.id.comment_btn_comment_notification);

        //Comment ui
        imageProfileComment = (CircleImageView) findViewById(R.id.image_profile_item_comment_notification);
        usernameComment = (TextView) findViewById(R.id.username_item_comment_notification);
        timeComment = (TextView) findViewById(R.id.time_item_comment_notification);
        textComment = (TextView) findViewById(R.id.comment_item_comment_notification);
        likeCountComment = (TextView) findViewById(R.id.likes_count_item_comment_notification);
        replyCountComment = (TextView) findViewById(R.id.replies_count_item_comment_notification);
        likeBtnComment = (ImageButton) findViewById(R.id.like_btn_item_comment_notification);
        likePressedBtnComment = (ImageButton) findViewById(R.id.like_pressed_btn_item_comment_notification);
        replyBtnComment = (ImageButton) findViewById(R.id.reply_btn_item_comment_notification);
        viewAllComments = (TextView) findViewById(R.id.view_all_comments_comment_notification);

        //Reply ui
        imageProfileReply = (CircleImageView) findViewById(R.id.image_profile_item_reply_comment_notification);
        usernameReply = (TextView) findViewById(R.id.username_item_reply_comment_notification);
        timeReply = (TextView) findViewById(R.id.time_item_reply_comment_notification);
        textReply = (TextView) findViewById(R.id.comment_item_reply_comment_notification);
        likeCountReply = (TextView) findViewById(R.id.likes_count_item_reply_comment_notification);
        likeBtnReply = (ImageButton) findViewById(R.id.like_btn_item_reply_comment_notification);
        likePressedBtnReply = (ImageButton) findViewById(R.id.like_pressed_btn_item_reply_comment_notification);
        replyTO = (TextView) findViewById(R.id.reply_item_reply_notification);
        progressBar.setVisibility(View.VISIBLE);
        master.setVisibility(View.GONE);





        if(commentID.equals("")){
            commentLayout.setVisibility(View.GONE);
            viewAllComments.setVisibility(View.GONE);
        }else {
            likePressedBtnComment.setVisibility(View.GONE);
            likeBtnComment.setVisibility(View.VISIBLE);
            getComment();
            getLikes(likeCountComment, likeBtnComment, likePressedBtnComment, FirebaseDatabase.getInstance().getReference()
                    .child("comments").child(postID).child(commentID).child("likes"));
            getRepliesCount();
        }

        if(replyID.equals("")){
            replyLayout.setVisibility(View.GONE);
        } else  {
            likePressedBtnReply.setVisibility(View.GONE);
            likeBtnReply.setVisibility(View.VISIBLE);
            getReply();
            getLikes(likeCountReply, likeBtnReply, likePressedBtnReply, FirebaseDatabase.getInstance().getReference()
                    .child("comments").child(postID).child(commentID).child("replies").child(replyID).child("likes"));
        }

        likeBtnPost.setVisibility(View.VISIBLE);
        likePressedBtnPost.setVisibility(View.GONE);
        getPost(postID);
        getLikes(likeCountPost, likeBtnPost, likePressedBtnPost, FirebaseDatabase.getInstance().getReference()
                .child("posts").child(postID).child("likes"));
        getCommentsCount();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });


        commentBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity();
            }
        });
        menuPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        shareBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        viewAllComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity();
            }
        });
        replyBtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyActivity();
            }
        });
        replyTO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyActivity();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        likeBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
        likePressedBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlikePost();
            }
        });
        likeCountPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(postID, "Likes");
            }
        });
        commentCountPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity();
            }
        });
        likeBtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeComment();
            }
        });
        likePressedBtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unLikeComment();
            }
        });
        likeCountComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(postID + "," + commentID, "Comment_Likes");
            }
        });
        replyCountComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyActivity();
            }
        });
        likeBtnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeReply();
            }
        });
        likePressedBtnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unLikeReply();
            }
        });
        likeCountReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(postID + "," + commentID + "," + replyID, "Reply_Likes");
            }
        });
        imageProfilePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(post.getPublisher());
            }
        });

        usernamePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(post.getPublisher());
            }
        });

        imageProfileComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(comment.getPublisher());
            }
        });

        usernameComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(comment.getPublisher());
            }
        });

        imageProfileReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(reply.getPublisher());
            }
        });

        usernameReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(reply.getPublisher());
            }
        });
    }

    private void getPost(final String postID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("posts")
                .orderByChild("postID")
                .equalTo(postID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    post = singleSnapshot.getValue(Post.class);
                }
                Glide.with(CommentNotificationActivity.this).load(post.getPostImage()).into(imagePost);
                timePost.setText(getTimeStamp(post.getDateCreated()));
                descPost.setText(post.getDescription());
                getUserInfo(imageProfilePost, usernamePost, post.getPublisher());
                progressBar.setVisibility(View.GONE);
                master.setVisibility(View.VISIBLE);

                
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCommentsCount(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentCountPost.setText(dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes(final TextView likesCount, final ImageButton likeBtn, final ImageButton likePressedBtn, DatabaseReference reference){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: ds: " +ds);
                    if(ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        likePressedBtn.setVisibility(View.VISIBLE);
                        likeBtn.setVisibility(View.GONE);
                    }
                    count++;
                }
                likesCount.setText(count + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTimeStamp(String  dateCreated){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = dateCreated;
        try{
            timeStamp = sdf.parse(photoTimeStamp);
            //Log.d(TAG, "getTimeStamp: timestamp " + timeStamp);
            difference = Math.round((today.getTime() - timeStamp.getTime())/1000 / 60);

        } catch (ParseException e){
            Log.e(TAG, "getTimeStamp: ParseExceptio: " + e.getMessage());
            difference = 0;
        }
        if(difference < 1){
            return "Just Now";
        } else if(difference <= 59) {
            return (difference + " mins ago");
        } else if(difference >= 60 && difference < 1440) {
            return ((Math.round(difference / 60)) + " hours ago");
        } else  {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM 'at' K:mm a");
            String res = simpleDateFormat.format(timeStamp.getTime());
            return res;
        }
    }

    private void getComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("comments")
                .child(postID)
                .orderByChild("comment_id")
                .equalTo(commentID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    comment = singleSnapshot.getValue(Comment.class);
                    Log.d(TAG, "onDataChange: comment is: " + comment);
                }
                textComment.setText(comment.getText());
                timeComment.setText(getTimeStamp(comment.getDate_created()));
                getUserInfo(imageProfileComment, usernameComment, comment.getPublisher());


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getReply(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("comments")
                .child(postID)
                .child(commentID)
                .child("replies")
                .orderByChild("reply_id")
                .equalTo(replyID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    reply = singleSnapshot.getValue(Reply.class);
                }
                textReply.setText(reply.getText());
                timeReply.setText(getTimeStamp(reply.getDate_created()));
                getUserInfo(imageProfileReply, usernameReply, reply.getPublisher());


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserInfo(final CircleImageView profileImage, final TextView username, String userID) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            User user;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    user = singleSnapshot.getValue(User.class);
                }
                Glide.with(CommentNotificationActivity.this).load(user.getProfile_image()).into(profileImage);
                username.setText(user.getUsername());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void commentActivity(){
        Intent intent = new Intent(CommentNotificationActivity.this, CommentActivity.class);
        intent.putExtra(POST, post);
        startActivity(intent);
    }

    private void replyActivity(){
        Intent intent = new Intent(CommentNotificationActivity.this, ReplyActivity.class);
        intent.putExtra(POST, post);
        intent.putExtra(COMMENT_ID, comment);
        startActivity(intent);
    }

    private void getRepliesCount(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID).child(commentID).child("replies");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                replyCountComment.setText(dataSnapshot.getChildrenCount() + " replies");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost(){
        FirebaseDatabase.getInstance().getReference().child("posts").child(postID)
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        likeBtnPost.setVisibility(View.GONE);
        likePressedBtnPost.setVisibility(View.VISIBLE);
        sendNotificationPost();
    }

    private void unlikePost(){
        FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostID())
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        likePressedBtnPost.setVisibility(View.GONE);
        likeBtnPost.setVisibility(View.VISIBLE);
    }

    private void likeComment(){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(commentID)
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).setValue(true);
        likeBtnComment.setVisibility(View.GONE);
        likePressedBtnComment.setVisibility(View.VISIBLE);
        sendNotificationComment(comment);
    }

    private void unLikeComment(){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(comment.getComment_id())
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).removeValue();
        likeBtnComment.setVisibility(View.VISIBLE);
        likePressedBtnComment.setVisibility(View.GONE);
    }

    private void likeReply(){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(commentID)
                .child("replies")
                .child(replyID)
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).setValue(true);
        likeBtnReply.setVisibility(View.GONE);
        likePressedBtnReply.setVisibility(View.VISIBLE);
        sendNotificationReply(reply);
    }

    private void unLikeReply(){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(commentID)
                .child("replies")
                .child(replyID)
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).removeValue();
        likeBtnReply.setVisibility(View.VISIBLE);
        likePressedBtnReply.setVisibility(View.GONE);
    }

    private void sendNotificationPost(){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , post.getPublisher(), "like_post", " liked your post.",
                    getTimestamp(), post.getPostID(), post.getPostImage(), notificationID);
            reference.child("notifications").child(post.getPublisher()).child(notificationID).setValue(notification);
        }
    }

    private void sendNotificationComment(Comment comment){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(comment.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , comment.getPublisher(), "like_comment", " liked your comment: " + comment.getText() + ".",
                    getTimestamp(), postID, comment.getComment_id(), post.getPostImage(), notificationID);
            reference.child("notifications").child(comment.getPublisher()).child(notificationID).setValue(notification);
        }
    }

    private void sendNotificationReply(Reply reply){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(reply.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , reply.getPublisher(), "like_reply", " liked your reply: " + reply.getText() + ".",
                    getTimestamp(), postID, commentID, reply.getReply_id(), post.getPostImage(), notificationID);
            reference.child("notifications").child(reply.getPublisher()).child(notificationID).setValue(notification);
        }
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void listActivity(String id, String title){
        Intent intent = new Intent(CommentNotificationActivity.this, ListActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        startActivity(intent);
    }

    private void profileFragment(String profileID){
        Intent intent = new Intent(CommentNotificationActivity.this, MainActivity.class);
        intent.putExtra(PROFILE_ID, profileID);
        startActivity(intent);

    }
}
