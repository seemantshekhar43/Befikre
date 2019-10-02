package com.seemantshekhar.befikre.Fragment;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.GridImageAdapter;
import com.seemantshekhar.befikre.ChatActivity;
import com.seemantshekhar.befikre.EditProfile;
import com.seemantshekhar.befikre.ExpandedGridView;
import com.seemantshekhar.befikre.ListActivity;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.Model.UserAccountSettings;
import com.seemantshekhar.befikre.R;
import com.seemantshekhar.befikre.UserPostActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    public static final int NUM_GRID_COLUMNS = 3;
    private static final String USER_SETTING = "userSetting";
    private static final String ACTIVITY_NUMBER = "activityNumber";
    private ImageView imageProfile;
    private TextView followers, following, username, fullName, bio, postCount;
    private ImageButton backBtn;
    private ImageButton settingsBtn;
    private Button messageBtn, followBtn;
    private ExpandedGridView postGrid;
    private static final String ID = "id";
    private static final String TITLE = "title";

    private Context mContext;
    private UserAccountSettings settings;

    private FirebaseUser currentUser;
    private String profileID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
//        profileID = prefs.getString("saved_user_id", "none");



        imageProfile = (ImageView) view.findViewById(R.id.image_profile_profile);
        followers = (TextView) view.findViewById(R.id.followers_profile);
        following = (TextView) view.findViewById(R.id.following_profile);
        username = (TextView) view.findViewById(R.id.username_profile);
        fullName = (TextView) view.findViewById(R.id.full_name_profile);
        bio = (TextView) view.findViewById(R.id.bio_profile);
        postCount = (TextView) view.findViewById(R.id.post_count_profile);
        backBtn = (ImageButton) view.findViewById(R.id.back_btn_profile);
        settingsBtn = (ImageButton) view.findViewById(R.id.settings_btn_profile);
        messageBtn =(Button) view.findViewById(R.id.message_btn_profile);
        followBtn = (Button) view.findViewById(R.id.follow_btn_profile);
        postGrid = (ExpandedGridView) view.findViewById(R.id.post_grid_profile);

        if(getArguments()!= null){
            profileID = getArguments().getString("profileID", currentUser.getUid());
            //Log.d(TAG, "onCreate: getIntent called " + profileID);
            isFollowing(profileID, followBtn);
        } else {
            profileID = currentUser.getUid();
            backBtn.setVisibility(View.GONE);
        }

        if(profileID.equals(currentUser.getUid())){
            messageBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.GONE);
            settingsBtn.setVisibility(View.VISIBLE);
        } else {
            isFollowing(profileID, followBtn);
            settingsBtn.setVisibility(View.GONE);
        }

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followBtn.getText().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUser.getUid())
                            .child("following").child(profileID).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileID)
                            .child("followers").child(currentUser.getUid()).setValue(true);
                    followBtn.setText(R.string.following);
                    sendNotification(profileID, currentUser.getUid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUser.getUid())
                            .child("following").child(profileID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileID)
                            .child("followers").child(currentUser.getUid()).removeValue();
                    followBtn.setText(R.string.follow);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager().getBackStackEntryCount() > 0){
                    getFragmentManager().popBackStack();
                } else {
                    getActivity().finish();
                }

            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageActivity();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsActivity();
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(profileID, "Followers");
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(profileID, "Following");
            }
        });
        userInfo();
        setUpPosts();
        getFollowersCount();
        getFollowingCount();

        return view;
    }

    private void isFollowing(final String userID, final Button btn){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(currentUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userID).exists()){
                    btn.setText(R.string.following);
                } else {
                    btn.setText(R.string.follow);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users_account_settings")
                .child(profileID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getContext() == null){
                    return;
                }
                settings = dataSnapshot.getValue(UserAccountSettings.class);
                Glide.with(getContext()).load(settings.getProfile_image()).into(imageProfile);
                username.setText("@" + settings.getUsername());
                fullName.setText(settings.getFull_name());
                bio.setText(settings.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileID).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileID).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpPosts(){
        final ArrayList<Post> posts = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("user_posts")
                .child(profileID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!isAdded()){
                    return;
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    posts.add(singleSnapshot.getValue(Post.class));
                }

                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                postGrid.setColumnWidth(imageWidth);

                ArrayList<String> imgURL = new ArrayList<>();
                for (int i = 0; i < posts.size(); i++){
                    imgURL.add(posts.get(i).getPostImage());
                }

                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "",
                        imgURL);
                postGrid.setAdapter(adapter);
                postGrid.setExpanded(true);

                postGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, "onItemClick: pressed");

                        Intent intent = new Intent(getActivity(), UserPostActivity.class);
                        intent.putExtra("postInfo", posts.get(position));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void listActivity(String id, String title){
        Intent intent = new Intent(getActivity(), ListActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        startActivity(intent);
    }

    private void settingsActivity(){
        Intent intent = new Intent(getActivity(), EditProfile.class);
        intent.putExtra(USER_SETTING, settings);
        intent.putExtra(ACTIVITY_NUMBER, "1");
        startActivity(intent);
    }

    private void messageActivity(){
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        startActivity(intent);
    }

    private void sendNotification(String profileID, String currentUserID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String notificationID = reference.child("notifications").push().getKey();
        Notification notification = new Notification(currentUserID, profileID, "follow", " is now following you.",
                getTimestamp(), notificationID);
        reference.child("notifications").child(profileID).child(notificationID).setValue(notification);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }
}
