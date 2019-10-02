package com.seemantshekhar.befikre.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Fragment.ProfileFragment;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public static final String PROFILE_ID = "profileID";
    private static final String TAG = "UserAdapter";
    private Context mContext;
    private List<User> mUserList;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUserList.get(i);

       // viewHolder.btnFollow.setVisibility(View.VISIBLE);
        viewHolder.username.setText(user.getUsername());
        viewHolder.fullName.setText(user.getFull_name());
        viewHolder.following.setVisibility(View.INVISIBLE);

        Glide.with(mContext).load(user.getProfile_image()).into(viewHolder.profileImage);
        isFollowing(user.getId(), viewHolder.following);

        if(user.getId().equals(firebaseUser.getUid())){
            viewHolder.following.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
//                editor.putString("saved_user_id",user.getId());
//                editor.apply();
                profileFragment(user.getId());
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUserList.size();
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
    private void profileFragment(String profileID) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(PROFILE_ID, profileID);
        mContext.startActivity(intent);
    }

}
