package com.seemantshekhar.befikre.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.CommentNotificationActivity;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;
import com.seemantshekhar.befikre.SquareImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private static final String PROFILE_ID = "profileID";
    private static final String POST_ID = "postID";
    private static final String COMMENT_ID = "commentID";
    private static final String REPLY_ID = "replyID";

    List<Notification> notificationList;
    Context context;

    public NotificationAdapter(Context context) {
        this.notificationList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<Notification> newNotification){
        int initSize = notificationList.size();
        notificationList.addAll(newNotification);
        notifyItemRangeChanged(initSize, newNotification.size());
    }

    public String getLastItemID(){
        return notificationList.get(notificationList.size() - 1).getNotification_id();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.notification_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Notification notification = notificationList.get(i);
        userInfo(notification, viewHolder.image_profile, viewHolder.notificationText);
        viewHolder.time.setText(getTimeStamp(notification.getTime()));
        Glide.with(context).load(notification.getPost_image()).into(viewHolder.image_post);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntent(notification);
            }
        });
        if(!notification.getIs_read()){
            markRead(notification.getNotification_id());
        }

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView notificationText;
        TextView time;
        CircleImageView image_profile;
        SquareImageView image_post;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationText = (TextView) itemView.findViewById(R.id.text_notification);
            time = (TextView) itemView.findViewById(R.id.time_notification);
            image_post = (SquareImageView) itemView.findViewById(R.id.image_post_notification);
            image_profile = (CircleImageView) itemView.findViewById(R.id.image_profile_notification);
        }
    }

    private String getTimeStamp(String notificationTime){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = notificationTime;
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

    private void userInfo(final Notification notification, final CircleImageView image_profile, final TextView notificationText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(notification.getSender());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            User user;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    user = singleSnapshot.getValue(User.class);
                }
                Glide.with(context).load(user.getProfile_image()).into(image_profile);
                String username = user.getUsername();
                String sentence = username + notification.getText();

                SpannableStringBuilder finalText = new SpannableStringBuilder(sentence);
                finalText.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationText.setText(finalText);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startIntent(Notification notification){

        switch (notification.getType()){
            case "follow":
                profileFragment(notification.getSender());
                break;

            case "like_post":
                commentNotificationActivity(notification.getPost_id(), "", "");
                break;

            case "like_comment":
                commentNotificationActivity(notification.getPost_id(), notification.getComment_id(), "");
                break;

            case "like_reply":
                commentNotificationActivity(notification.getPost_id(), notification.getComment_id(), notification.getReply_id());
                break;

            case "comment":
                commentNotificationActivity(notification.getPost_id(), notification.getComment_id(), "");
                break;

            case "reply":
                commentNotificationActivity(notification.getPost_id(), notification.getComment_id(), notification.getReply_id());
                break;
        }

    }

    private void profileFragment(String profileID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(PROFILE_ID, profileID);
        context.startActivity(intent);
    }

    private void commentNotificationActivity(String postID, String commentID, String replyID){
        Intent intent = new Intent(context, CommentNotificationActivity.class);
        intent.putExtra(POST_ID, postID);
        intent.putExtra(COMMENT_ID, commentID);
        intent.putExtra(REPLY_ID, replyID);
        context.startActivity(intent);
    }

    private void markRead(String notificationID){
        FirebaseDatabase.getInstance().getReference().child("notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(notificationID).child("is_read").setValue(true);
    }
}
