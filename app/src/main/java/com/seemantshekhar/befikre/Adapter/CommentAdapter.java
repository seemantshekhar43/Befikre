package com.seemantshekhar.befikre.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.ListActivity;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.Comment;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;
import com.seemantshekhar.befikre.ReplyActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private static final String TAG = "CommentAdapter";
    private static final String POST = "post";
    private static final String COMMENT_ID = "commentID";
    private static final String PROFILE_ID = "profileID";
    private static final String ID = "id";
    private static final String TITLE = "title";

    private Context mContext;
    private List<Comment> mList;
    private String postID;
    private Post post;

    public CommentAdapter(Context mContext, List<Comment> mList, Post post) {
        this.mContext = mContext;
        this.mList = mList;
        this.post = post;
        this.postID = post.getPostID();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getComment_id().hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, viewGroup, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Comment comment = mList.get(i);

        viewHolder.commentText.setText(comment.getText());
        viewHolder.time.setText(getTimeStamp(comment));
        getUserInfo(viewHolder.imageProfile, viewHolder.username, comment.getPublisher());
        getLikes(viewHolder.likesCount, comment, viewHolder.likeBtn, viewHolder.likePressedBtn);
        getReplies(viewHolder.repliesCount, comment);
        viewHolder.likePressedBtn.setVisibility(View.GONE);
        viewHolder.likeBtn.setVisibility(View.VISIBLE);
        getLikes(viewHolder.likesCount, comment, viewHolder.likeBtn, viewHolder.likePressedBtn);

        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(comment.getPublisher());
            }
        });

        viewHolder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(comment.getPublisher());
            }
        });

        viewHolder.likesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(postID + "," + comment.getComment_id(), "Comment_Likes");
            }
        });

        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeComment(viewHolder.likeBtn, viewHolder.likePressedBtn, comment);
            }
        });

        viewHolder.likePressedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unLikeComment(viewHolder.likeBtn, viewHolder.likePressedBtn, comment);
            }
        });

        viewHolder.replyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyActivity(comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        TextView time;
        TextView commentText;
        TextView likesCount;
        TextView repliesCount;

        CircleImageView imageProfile;
        ImageButton likeBtn;
        ImageButton likePressedBtn;
        ImageButton replyBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.username_item_comment);
            time = (TextView) itemView.findViewById(R.id.time_item_comment);
            commentText = (TextView) itemView.findViewById(R.id.comment_item_comment);
            likesCount = (TextView) itemView.findViewById(R.id.likes_count_item_comment);
            repliesCount = (TextView) itemView.findViewById(R.id.replies_count_item_comment);
            imageProfile = (CircleImageView) itemView.findViewById(R.id.image_profile_item_comment);
            likeBtn = (ImageButton) itemView.findViewById(R.id.like_btn_item_comment);
            likePressedBtn = (ImageButton) itemView.findViewById(R.id.like_pressed_btn_item_comment);
            replyBtn = (ImageButton) itemView.findViewById(R.id.reply_btn_item_comment);

        }


    }

    private void getUserInfo(final CircleImageView profileImage, final TextView username, String publisherID) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(publisherID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            User user;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    user = singleSnapshot.getValue(User.class);
                }
                Glide.with(mContext).load(user.getProfile_image()).into(profileImage);
                username.setText(user.getUsername());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTimeStamp(Comment comment){
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

    private void getLikes(final TextView likesCount, final Comment comment, final ImageButton likeBtn, final ImageButton likePressedBtn){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
        .child("comments").child(postID).child(comment.getComment_id()).child("likes");

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
                        Log.d(TAG, "onDataChange: called for comment ID" + comment.getComment_id());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getReplies(final TextView repliesCount, Comment comment){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID).child(comment.getComment_id()).child("replies");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               repliesCount.setText(dataSnapshot.getChildrenCount() + " replies");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likeComment(ImageButton likeBtn, ImageButton likePressedBtn, Comment comment){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(comment.getComment_id())
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).setValue(true);
        likeBtn.setVisibility(View.GONE);
        likePressedBtn.setVisibility(View.VISIBLE);
        sendNotification(comment);
    }

    private void unLikeComment(ImageButton likeBtn, ImageButton likePressedBtn, Comment comment){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(comment.getComment_id())
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).removeValue();
        likeBtn.setVisibility(View.VISIBLE);
        likePressedBtn.setVisibility(View.GONE);
    }

    private void profileFragment(String profileID){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(PROFILE_ID, profileID);
        mContext.startActivity(intent);

    }

    private void replyActivity(Comment comment){
        Intent intent = new Intent(mContext, ReplyActivity.class);
        intent.putExtra(POST, post);
        intent.putExtra(COMMENT_ID, comment);
        mContext.startActivity(intent);
    }

    private void listActivity(String id, String title){
        Intent intent = new Intent(mContext, ListActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        mContext.startActivity(intent);
    }

    private void sendNotification(Comment comment){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(comment.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , comment.getPublisher(), "like_comment", " liked your comment: " + comment.getText() + ".",
                    getTimestamp(), postID, comment.getComment_id(), post.getPostImage(), notificationID);
            reference.child("notifications").child(comment.getPublisher()).child(notificationID).setValue(notification);
        }
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

}
