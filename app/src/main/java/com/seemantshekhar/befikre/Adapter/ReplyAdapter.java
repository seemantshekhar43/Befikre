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
import com.seemantshekhar.befikre.Model.Reply;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder>{
    private static final String TAG = "ReplyAdapter";
    public static final String PROFILE_ID = "profileID";
    private static final String ID = "id";
    private static final String TITLE = "title";

    private Context mContext;
    private List<Reply> mList;
    private String postID;
    private String commentID;
    private Post post;

    public ReplyAdapter(Context mContext, List<Reply> mList, Post post, String commentID) {
        this.mContext = mContext;
        this.mList = mList;
        this.post = post;
        this.postID = post.getPostID();
        this.commentID = commentID;
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getReply_id().hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.reply_item, viewGroup, false);
        return new ReplyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder viewHolder, int i) {

        final Reply reply = mList.get(i);

        viewHolder.replyText.setText(reply.getText());
        viewHolder.time.setText(getTimeStamp(reply));
        getUserInfo(viewHolder.imageProfile, viewHolder.username, reply.getPublisher());
        getLikes(viewHolder.likesCount, reply, viewHolder.likeBtn, viewHolder.likePressedBtn);

        viewHolder.likePressedBtn.setVisibility(View.GONE);
        viewHolder.likeBtn.setVisibility(View.VISIBLE);

        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeComment(viewHolder.likeBtn, viewHolder.likePressedBtn, reply);
            }
        });

        viewHolder.likePressedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unLikeComment(viewHolder.likeBtn, viewHolder.likePressedBtn, reply);
            }
        });

        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(reply.getPublisher());
            }
        });

        viewHolder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(reply.getPublisher());
            }
        });

        viewHolder.likesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(postID + "," + commentID + "," + reply.getReply_id(), "Reply_Likes");
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
        TextView replyText;
        TextView likesCount;

        CircleImageView imageProfile;
        ImageButton likeBtn;
        ImageButton likePressedBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.username_item_reply);
            time = (TextView) itemView.findViewById(R.id.time_item_reply);
            replyText = (TextView) itemView.findViewById(R.id.comment_item_reply);
            likesCount = (TextView) itemView.findViewById(R.id.likes_count_item_reply);
            imageProfile = (CircleImageView) itemView.findViewById(R.id.image_profile_item_reply);
            likeBtn = (ImageButton) itemView.findViewById(R.id.like_btn_item_reply);
            likePressedBtn = (ImageButton) itemView.findViewById(R.id.like_pressed_btn_item_reply);
        }
    }

    private String getTimeStamp(Reply reply){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = reply.getDate_created();
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

    private void getLikes(final TextView likesCount, final Reply reply, final ImageButton likeBtn, final ImageButton likePressedBtn){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID).child(commentID)
                .child("replies")
                .child(reply.getReply_id()).child("likes");

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
                Log.d(TAG, "onDataChange: called for comment ID" + reply.getReply_id());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likeComment(ImageButton likeBtn, ImageButton likePressedBtn, Reply reply){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(commentID)
                .child("replies")
                .child(reply.getReply_id())
                .child("likes").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).setValue(true);
        likeBtn.setVisibility(View.GONE);
        likePressedBtn.setVisibility(View.VISIBLE);
        sendNotification(reply);
    }

    private void unLikeComment(ImageButton likeBtn, ImageButton likePressedBtn, Reply reply){
        FirebaseDatabase.getInstance().getReference().child("comments").child(postID)
                .child(commentID)
                .child("replies")
                .child(reply.getReply_id())
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
    private void listActivity(String id, String title){
        Intent intent = new Intent(mContext, ListActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        mContext.startActivity(intent);
    }

    private void sendNotification(Reply reply){
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
}
