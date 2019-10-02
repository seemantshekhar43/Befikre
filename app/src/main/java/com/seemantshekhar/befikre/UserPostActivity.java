package com.seemantshekhar.befikre;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.GridImageAdapter;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPostActivity extends AppCompatActivity {

    private static final String TAG = "UserPostActivity";
    public static final String POST = "post";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private TextView username;
    private TextView time;
    private TextView desc;
    private TextView likes;
    private TextView comments;
    private ImageButton back;
    private CircleImageView imageProfile;
    private ImageView imagePost;
    private ImageButton likeBtn;
    private ImageButton likePressedBtn;
    private ImageButton shareBtn;
    private ImageButton commentBtn;
    private Post post;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post);

        username = (TextView) findViewById(R.id.username_user_post);
        time = (TextView) findViewById(R.id.time_user_post);
        desc = (TextView) findViewById(R.id.post_desc_user_post);
        likes = (TextView) findViewById(R.id.post_like_count_user_post);
        comments= (TextView) findViewById(R.id.post_comment_count_user_post);
        back = (ImageButton) findViewById(R.id.back_btn_user_post);
        imageProfile = (CircleImageView) findViewById(R.id.image_profile_user_post);
        imagePost = (ImageView) findViewById(R.id.post_image_user_post);
        likeBtn = (ImageButton) findViewById(R.id.like_btn_user_post);
        likePressedBtn = (ImageButton) findViewById(R.id.like_btn_pressed_user_post);
        shareBtn = (ImageButton) findViewById(R.id.share_btn_user_post);
        commentBtn = (ImageButton) findViewById(R.id.comment_btn_user_post);

        if(getIntent().getExtras() != null){
            post = getIntent().getExtras().getParcelable("postInfo");
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        likePressedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlikePost();
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity();
            }
        });

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity();
            }
        });

        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(post.getPostID(), "Likes");
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        likePressedBtn.setVisibility(View.GONE);
        setInfo();
        getLikes();
        getComments();

    }

    private void setInfo() {
        desc.setText(post.getDescription());
        time.setText(getTimeStamp());
        Glide.with(UserPostActivity.this).load(post.getPostImage()).into(imagePost);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(post.getPublisher());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        user = singleSnapshot.getValue(User.class);
                }
                Log.d(TAG, "onDataChange: user " + user.toString());
                Glide.with(UserPostActivity.this).load(user.getProfile_image()).into(imageProfile);
                username.setText(user.getUsername());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private String getTimeStamp(){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = post.getDateCreated();
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

    private void getLikes(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(post.getPostID()).child("likes");
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
                likes.setText(count + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(post.getPostID());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText(dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost(){
        FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostID())
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        likeBtn.setVisibility(View.GONE);
        likePressedBtn.setVisibility(View.VISIBLE);
        sendNotification();
    }

    private void unlikePost(){
        FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostID())
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        likePressedBtn.setVisibility(View.GONE);
        likeBtn.setVisibility(View.VISIBLE);
    }

    private void commentActivity(){
        Intent intent = new Intent(UserPostActivity.this, CommentActivity.class);
        intent.putExtra(POST, post);
        startActivity(intent);
    }

    private void listActivity(String id, String title){
        Intent intent = new Intent(UserPostActivity.this, ListActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        startActivity(intent);
    }

    private void sendNotification(){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , post.getPublisher(), "like_post", " liked your post.",
                    getTimestamp(), post.getPostID(), post.getPostImage(), notificationID);
            reference.child("notifications").child(post.getPublisher()).child(notificationID).setValue(notification);
        }
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }
}
