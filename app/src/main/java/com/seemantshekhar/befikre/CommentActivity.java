package com.seemantshekhar.befikre;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.CommentAdapter;
import com.seemantshekhar.befikre.Model.Comment;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.Reply;
import com.seemantshekhar.befikre.Model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    EditText commentText;
    ImageButton sendBtn;
    ImageButton backBtn;
    CircleImageView imageProfile;
    Post post;
    String postID;

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        if(getIntent().getExtras()!= null){
            post = getIntent().getExtras().getParcelable("post");
            postID = post.getPostID();
        }

        recyclerView = findViewById(R.id.recycler_view_comment);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, post);
        commentAdapter.setHasStableIds(true);
        recyclerView.setAdapter(commentAdapter);

        commentText = (EditText) findViewById(R.id.comment_comment);
        sendBtn = (ImageButton) findViewById(R.id.send_comment);
        backBtn = (ImageButton) findViewById(R.id.back_btn_comment);
        imageProfile = (CircleImageView) findViewById(R.id.image_profile_comment);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = CommentActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if(commentText.getText().toString().equals("")){
                    Toast.makeText(CommentActivity.this, "Empty Comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
                // Check if no view has focus:

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getUserInfo();
        readComments();


    }

    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("comments").child(postID);
        String text = commentText.getText().toString();
        String commentID = reference.push().getKey();
        String time = getTimestamp();

        Comment comment = new Comment(text, commentID, FirebaseAuth.getInstance().getCurrentUser().getUid(), time);
        reference.child(commentID).setValue(comment);
        //recyclerView.scrollToPosition(commentAdapter.getItemCount() - 1);
        commentText.setText("");
        sendNotification(comment);

    }

    private void getUserInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(CommentActivity.this).load(user.getProfile_image()).into(imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments")
                .child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int size = commentList.size();
                commentList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
                if (size != commentList.size()){
                    recyclerView.scrollToPosition(commentAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void sendNotification(Comment comment){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , post.getPublisher(), "comment", " commented on your post: " + comment.getText() + ".",
                    getTimestamp(), postID, comment.getComment_id(), post.getPostImage(), notificationID);
            reference.child("notifications").child(post.getPublisher()).child(notificationID).setValue(notification);
        }
    }
}
