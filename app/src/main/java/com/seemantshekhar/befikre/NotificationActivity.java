package com.seemantshekhar.befikre;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.NotificationAdapter;
import com.seemantshekhar.befikre.Model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = "NotificationActivity";
    public static final int ITEM_LOAD_COUNT = 11;
    private int totalItem = 0;
    private int lastVisibleItem;
    private NotificationAdapter notificationAdapter;
    private boolean isLoading = false;
    private boolean isMaxData = false;

    private String last_node = "";
    private String last_key = "";


    private ImageButton backBtn;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        backBtn = (ImageButton) findViewById(R.id.back_btn_notification);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_notification);

        getLastKey();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        notificationAdapter = new NotificationAdapter(this);
        recyclerView.setAdapter(notificationAdapter);

        getNotifications();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItem = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if(!isLoading && totalItem <= (lastVisibleItem + ITEM_LOAD_COUNT)){
                    getNotifications();
                    isLoading = true;
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getLastKey(){
        Query query = FirebaseDatabase.getInstance().getReference().child("notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByKey()
                .limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    last_key = singleSnapshot.getKey();
                    Log.d(TAG, "onDataChange: last key is: " + last_key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNotifications(){
        if(!isMaxData){
            Query query;
            if(TextUtils.isEmpty(last_node)) {
                query = FirebaseDatabase.getInstance().getReference()
                        .child("notifications")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderByKey()
                        .limitToLast(ITEM_LOAD_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference()
                        .child("notifications")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderByKey()
                        .endAt(last_node)
                        .limitToLast(ITEM_LOAD_COUNT);
            }

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren() && !last_node.equals("end")){
                        List<Notification> newNotifications = new ArrayList<>();
                        for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                            newNotifications.add(singleSnapshot.getValue(Notification.class));
                            Log.d(TAG, "onDataChange: singlesnapshot" + singleSnapshot);
                        }
                        Collections.reverse(newNotifications);
                        last_node = newNotifications.get(newNotifications.size() - 1).getNotification_id();

                        if(!last_node.equals(last_key)){
                            newNotifications.remove(newNotifications.size() - 1);
                        } else {
                            last_node = "end";
                        }
                        notificationAdapter.addAll(newNotifications);
                        isLoading = false;
                    } else {
                        isLoading = false;
                        isMaxData = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isLoading = false;
                }
            });
        }
    }
}
