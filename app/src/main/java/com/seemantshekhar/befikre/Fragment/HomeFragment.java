package com.seemantshekhar.befikre.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.PostAdapter;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.NotificationActivity;
import com.seemantshekhar.befikre.PostActivity;
import com.seemantshekhar.befikre.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Post> paginatedPostList;
    private int mResult;
    private ProgressBar progressBar;

    private boolean isScrolling = false;
    private int currentItems;
    private int totalItems;
    private int scrollOutItems;

    private List<String> followingList;

    ImageButton addPost;
    ImageButton notificationBtn;
    CircleImageView notification_circle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        addPost = (ImageButton) view.findViewById(R.id.add_post_btn_home);
        notificationBtn = (ImageButton) view.findViewById(R.id.notfication_btn_home);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular_home);
        notification_circle = (CircleImageView) view.findViewById(R.id.circle_notification_home);
        notification_circle.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.recyler_view_home);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        paginatedPostList = new ArrayList<>();
        followingList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), paginatedPostList);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = linearLayoutManager.getChildCount();
                totalItems = linearLayoutManager.getItemCount();
                scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(isScrolling && (currentItems + scrollOutItems == totalItems)){
                    Log.d(TAG, "onScrolled: " + currentItems + " " + scrollOutItems + " " + totalItems);
                    isScrolling = false;
                    displayMorePosts();
                }

            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG, "onClick: pressed");
               Intent intent = new Intent(getActivity(), PostActivity.class);
               startActivity(intent);
           }
       });

        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationActivity();
                notification_circle.setVisibility(View.GONE);
            }
        });
        getFollowing();
        notificationListener();
        return view;

    }

    private void getFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                   followingList.add(singleSnapshot.getKey());
                }
                followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                Log.d(TAG, "onDataChange: following list is: " + followingList);
                getPost();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPost(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < followingList.size(); i++){
            final int count = i;
            Query query = reference
                    .child("user_posts")
                    .child(followingList.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        postList.add(singleSnapshot.getValue(Post.class));
                    }
                    Log.d(TAG, "onDataChange: postlist is: " + postList);
                    if( count >= followingList.size() - 1){
                        sortByDate();
                    }

                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        sortByDate();
    }

    private void sortByDate(){
        Log.d(TAG, "sortByDate: called");
        if(postList != null){
            Collections.sort(postList, new Comparator<Post>() {
                @Override
                public int compare(Post o1, Post o2) {
                    return o2.getDateCreated().compareTo(o1.getDateCreated());
                }
            });

            displayPosts();
        }
    }
    private void notificationActivity(){
        Intent intent = new Intent(getActivity(), NotificationActivity.class);
        startActivity(intent);
    }




    private void displayPosts(){
        Log.d(TAG, "displayPosts: called");
        int iteration = postList.size();
        if(iteration > 10){
            iteration = 10;
        }
        mResult = 10;
        for(int i = 0; i <iteration; i++){
            paginatedPostList.add(postList.get(i));
        }
//        Log.d(TAG, "displayPosts: paginatedPostList" + paginatedPostList);
        progressBar.setVisibility(View.GONE);
        recyclerView.setAdapter(postAdapter);
        //postAdapter.notifyDataSetChanged();
    }

    public void displayMorePosts(){
        Log.d(TAG, "displayMorePosts: called");
        if(postList.size() > mResult){

            int iteration;
            if(postList.size() > (mResult + 10)){
                iteration = 10;
            } else {
                iteration = postList.size() - mResult;
            }
            for(int i = mResult; i < mResult + iteration; i++){
                paginatedPostList.add(postList.get(i));
            }
            mResult += iteration;
            postAdapter.notifyDataSetChanged();
        }
    }

    private void notificationListener(){
        Query query = FirebaseDatabase.getInstance().getReference().child("notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByKey()
                .limitToLast(1);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(!singleSnapshot.getValue(Notification.class).getIs_read()){
                        notification_circle.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "onDataChange: last key is: " + singleSnapshot.getValue(Notification.class).getIs_read());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
