package com.seemantshekhar.befikre;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServic";
    public static final String CHANNEL_ID = "BefikreNotification";
    private static final String PROFILE_ID = "profileID";
    private static final String POST_ID = "postID";
    private static final String COMMENT_ID = "commentID";
    private static final String REPLY_ID = "replyID";

    private String comment_id = "";
    private String reply_id = "";
    private String post_id = "";
    private String sender  = "";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String type = remoteMessage.getData().get("type");
        if(remoteMessage.getData().get("commentID") != null){
            comment_id = remoteMessage.getData().get("commentID");
        }
        if(remoteMessage.getData().get("replyID") != null){
            reply_id = remoteMessage.getData().get("replyID");
        }
        if(remoteMessage.getData().get("postID") != null){
            post_id = remoteMessage.getData().get("postID");
        }
        if(remoteMessage.getData().get("profileID") != null){
            sender = remoteMessage.getData().get("profileID");
        }



        Log.d(TAG, "onMessageReceived: " + comment_id + " " + reply_id + " " + post_id + " " + sender + " " + type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BefikreNotification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(click_action);
        if(type.equals("follow")){
            resultIntent.putExtra(PROFILE_ID, sender);
        } else {
            resultIntent.putExtra(POST_ID, post_id);
            resultIntent.putExtra(COMMENT_ID, comment_id);
            resultIntent.putExtra(REPLY_ID, reply_id);
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        //int notification_id = (int) System.currentTimeMillis();
        int notification_id = 001;
        NotificationManagerCompat manager = NotificationManagerCompat.from(FirebaseMessagingService.this);
        manager.notify(notification_id, builder.build());

    }
}
