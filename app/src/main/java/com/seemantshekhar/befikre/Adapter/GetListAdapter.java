package com.seemantshekhar.befikre.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.seemantshekhar.befikre.Fragment.ProfileFragment;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GetListAdapter extends RecyclerView.Adapter<GetListAdapter.ViewHolder> {
    private static final String PROFILE_ID = "profileID";
    private static final String TAG = "GetListAdapter";
    private Context mContext;
    private List<String> idList;

    private FirebaseUser firebaseUser;

    public GetListAdapter(Context mContext, List<String> idList) {
        this.mContext = mContext;
        this.idList = idList;
    }

    @NonNull
    @Override
    public GetListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreateViewHolder: called");
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new GetListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GetListAdapter.ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String id = idList.get(i);
        Log.d(TAG, "onBindViewHolder: id is" + id);
       getUser(id, viewHolder.username, viewHolder.fullName, viewHolder.profileImage);

        // viewHolder.btnFollow.setVisibility(View.VISIBLE);
        viewHolder.following.setVisibility(View.INVISIBLE);
        isFollowing(id, viewHolder.following);

        if(id.equals(firebaseUser.getUid())){
            viewHolder.following.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(id);
            }
        });


    }

    @Override
    public int getItemCount() {
        return idList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public TextView fullName;
        public TextView following;
        public CircleImageView profileImage;
        //public Button btnFollow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_search);
            fullName = itemView.findViewById(R.id.full_name_search);
            profileImage = itemView.findViewById(R.id.image_profile_search);
            following = itemView.findViewById(R.id.following_item_user);
            //btnFollow = itemView.findViewById(R.id.follow_btn_search);

        }
    }

    private void getUser(String id, final TextView username, final TextView fullName, final CircleImageView imageProfile){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = new User();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    user = singleSnapshot.getValue(User.class);
                }
                Log.d(TAG, "onDataChange: user " + user.toString());
                Glide.with(mContext).load(user.getProfile_image()).into(imageProfile);
                username.setText(user.getUsername());
                fullName.setText(user.getFull_name());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void isFollowing(final String userID, final TextView following){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userID).exists()){
                    following.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void profileFragment(String profileID){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(PROFILE_ID, profileID);
        mContext.startActivity(intent);

    }
}
