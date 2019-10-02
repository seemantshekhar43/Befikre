package com.seemantshekhar.befikre;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.GetListAdapter;
import com.seemantshekhar.befikre.Adapter.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";
    private RecyclerView recyclerView;
    private GetListAdapter getListAdapter;
    private List<String> idList;
    private ImageButton backBtn;
    private TextView activityTitle;

    private String id;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (getIntent().getExtras() != null) {
            id = getIntent().getExtras().getString("id");
            title = getIntent().getExtras().getString("title");
        }

        backBtn = (ImageButton) findViewById(R.id.back_btn_list);
        activityTitle = (TextView) findViewById(R.id.title_list);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        idList = new ArrayList<>();
        getListAdapter = new GetListAdapter(ListActivity.this, idList);
        recyclerView.setAdapter(getListAdapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        switch (title){
            case "Likes":
                activityTitle.setText(title);
                getLikes();
                break;

            case "Followers":
                activityTitle.setText(title);
                getFollowers();
                break;
            case "Following":
                activityTitle.setText(title);
                getFollowing();
                break;
            case "Comment_Likes":
                activityTitle.setText(R.string.likes);
                String[] ids = id.split(",");
                getCommentLikes(ids[0], ids[1]);
                break;

            case "Reply_Likes":
                activityTitle.setText(R.string.likes);
                String[] replyIds = id.split(",");
                getReplyLikes(replyIds[0], replyIds[1], replyIds[2]);
                break;
        }
    }

    private void getReplyLikes(String postID, String commentID, String replyID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID).child(commentID)
                .child("replies")
                .child(replyID).child("likes");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                getListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCommentLikes(String postID, String commentID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(postID).child(commentID)
                .child("likes");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                getListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Follow").child(id)
                .child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                getListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Follow").child(id)
                .child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                getListAdapter.notifyDataSetChanged();
                Log.d(TAG, "onDataChange: followers list: " + idList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("posts").child(id)
                .child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                getListAdapter.notifyDataSetChanged();
                Log.d(TAG, "onDataChange: id list is: " + idList);
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
}
