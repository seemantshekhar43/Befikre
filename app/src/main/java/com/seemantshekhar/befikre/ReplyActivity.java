package com.seemantshekhar.befikre;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.CommentAdapter;
import com.seemantshekhar.befikre.Adapter.ReplyAdapter;
import com.seemantshekhar.befikre.Model.Comment;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.Reply;
import com.seemantshekhar.befikre.Model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyActivity extends AppCompatActivity {
    private static final String TAG = "ReplyActivity";

    EditText replyText;
    ImageButton sendBtn;
    ImageButton backBtn;
    CircleImageView imageProfile;
    private String postID;
    private String commentID;
    private Comment comment;
    private Post post;

    private TextView pUsername;
    private TextView pCommentText;
    private TextView pTime;
    private CircleImageView pImageProfile;


    private RecyclerView recyclerView;
    private ReplyAdapter replyAdapter;
    private List<Reply> replyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        if (getIntent().getExtras() != null) {
            post = getIntent().getExtras().getParcelable("post");
            postID = post.getPostID();
            comment = getIntent().getExtras().getParcelable("commentID");
            commentID = comment.getComment_id();
        }

        recyclerView = findViewById(R.id.recycler_view_reply);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        replyList = new ArrayList<>();
        replyAdapter = new ReplyAdapter(this, replyList, post, commentID);
        replyAdapter.setHasStableIds(true);
        recyclerView.setAdapter(replyAdapter);


        replyText = (EditText) findViewById(R.id.comment_reply);
        imageProfile = (CircleImageView) findViewById(R.id.image_profile_reply);
        sendBtn = (ImageButton) findViewById(R.id.send_reply);
        backBtn = (ImageButton) findViewById(R.id.back_btn_reply);

        pCommentText = (TextView) findViewById(R.id.comment_activity_reply);
        pImageProfile = (CircleImageView) findViewById(R.id.image_profile_activity_reply);
        pTime = (TextView) findViewById(R.id.time_activity_reply);
        pUsername = (TextView) findViewById(R.id.username_reply_activity);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = ReplyActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (replyText.getText().toString().equals("")) {
                    Toast.makeText(ReplyActivity.this, "Empty Reply", Toast.LENGTH_SHORT).show();
                } else {
                    addReply();
                }
            }
        });

        getPublisherInfo();
        getUserInfo();
        readReplies();

    }

    private void addReply() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("comments").child(postID)
                .child(commentID).child("replies");
        String text = replyText.getText().toString();
        String replyID = reference.push().getKey();
        String time = getTimestamp();

        Reply reply = new Reply(text, replyID, FirebaseAuth.getInstance().getCurrentUser().getUid(), time);
        reference.child(replyID).setValue(reply);
        replyText.setText("");
        sendNotification(reply);
        //recyclerView.scrollToPosition(replyAdapter.getItemCount() - 1);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(ReplyActivity.this).load(user.getProfile_image()).into(imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readReplies(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments")
                .child(postID).child(commentID).child("replies");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int size  = replyList.size();
                replyList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Reply reply = ds.getValue(Reply.class);
                    replyList.add(reply);
                }
                replyAdapter.notifyDataSetChanged();
                if(size != replyList.size())
                {
                    recyclerView.scrollToPosition(replyAdapter.getItemCount() - 1);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private void getPublisherInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(comment.getPublisher());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            User user;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    user = singleSnapshot.getValue(User.class);
                }
                Glide.with(ReplyActivity.this).load(user.getProfile_image()).into(pImageProfile);
                pUsername.setText(user.getUsername());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        pCommentText.setText(comment.getText());
        pTime.setText(setTimeStamp(comment));
    }

    private String setTimeStamp(Comment comment){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = comment.getDate_created();
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
    private void sendNotification(Reply reply){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(comment.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , comment.getPublisher(), "reply", " replied to your comment: " + reply.getText() + ".",
                    getTimestamp(), postID, comment.getComment_id(), reply.getReply_id(), post.getPostImage(), notificationID);
            reference.child("notifications").child(comment.getPublisher()).child(notificationID).setValue(notification);
        }
    }
}
