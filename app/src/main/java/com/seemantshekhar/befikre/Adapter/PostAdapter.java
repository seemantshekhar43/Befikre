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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.CommentActivity;
import com.seemantshekhar.befikre.ListActivity;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.Notification;
import com.seemantshekhar.befikre.Model.Post;
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

public class PostAdapter  extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";
    private static final String POST = "post";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String PROFILE_ID = "profileID";

    public Context mContext;
    public List<Post> mList;

    public PostAdapter(Context mContext, List<Post> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Post post = mList.get(i);
        viewHolder.desc.setVisibility(View.VISIBLE);
        if(post.getDescription().equals("")){
            viewHolder.desc.setVisibility(View.GONE);
        } else {
            viewHolder.desc.setText(post.getDescription());
        }
        viewHolder.time.setText(getTimeStamp(post));
        Glide.with(mContext).load(post.getPostImage()).into(viewHolder.imagePost);
        viewHolder.likePressedBtn.setVisibility(View.GONE);
        viewHolder.likeBtn.setVisibility(View.VISIBLE);
        setInfo(post, viewHolder.username, viewHolder.imageProfile);
        getLikes(post,viewHolder.likePressedBtn, viewHolder.likeBtn, viewHolder.likes);
        getComments(post, viewHolder.comments);

        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(post, viewHolder.likePressedBtn, viewHolder.likeBtn);
            }
        });

        viewHolder.likePressedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlikePost(post, viewHolder.likePressedBtn, viewHolder.likeBtn);
            }
        });

        viewHolder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity(post.getPostID(), "Likes");
            }
        });

        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity(post);
            }
        });

        viewHolder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentActivity(post);
            }
        });

        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(post.getPublisher());
            }
        });

        viewHolder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFragment(post.getPublisher());
            }
        });


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private static final String TAG = "UserPostActivity";
        public static final String POST_ID = "postID";
        private TextView username;
        private TextView time;
        private TextView desc;
        private TextView likes;
        private TextView comments;
        private CircleImageView imageProfile;
        private ImageView imagePost;
        private ImageButton likeBtn;
        private ImageButton likePressedBtn;
        private ImageButton shareBtn;
        private ImageButton commentBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username_post);
            time = (TextView) itemView.findViewById(R.id.time_post);
            desc = (TextView) itemView.findViewById(R.id.post_desc_post);
            likes = (TextView) itemView.findViewById(R.id.post_like_count_post);
            comments= (TextView) itemView.findViewById(R.id.post_comment_count_post);
            imageProfile = (CircleImageView) itemView.findViewById(R.id.image_profile_post);
            imagePost = (ImageView) itemView.findViewById(R.id.post_image_post);
            likeBtn = (ImageButton) itemView.findViewById(R.id.like_btn_post);
            likePressedBtn = (ImageButton) itemView.findViewById(R.id.like_btn_pressed_post);
            shareBtn = (ImageButton) itemView.findViewById(R.id.share_btn_post);
            commentBtn = (ImageButton) itemView.findViewById(R.id.comment_btn_post);

        }
    }

    private void setInfo(Post post, final TextView username, final CircleImageView imageProfile) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child("users")
                .orderByChild("id")
                .equalTo(post.getPublisher());
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
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getTimeStamp(Post post){
        int difference = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp = today;
        final String photoTimeStamp = post.getDateCreated();
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

    private void getLikes(Post post, final ImageButton likePressedBtn, final ImageButton likeBtn, final TextView likes){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(post.getPostID()).child("likes");
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
                likes.setText(count + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(Post post, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("comments").child(post.getPostID());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText(dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost(Post post, ImageButton likePressedBtn, ImageButton likeBtn){
        FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostID())
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        likeBtn.setVisibility(View.GONE);
        likePressedBtn.setVisibility(View.VISIBLE);
        sendNotification(post);
    }

    private void unlikePost(Post post, ImageButton likePressedBtn, ImageButton likeBtn){
        FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostID())
                .child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        likePressedBtn.setVisibility(View.GONE);
        likeBtn.setVisibility(View.VISIBLE);
    }

    private void commentActivity(Post post){
        Intent intent = new Intent(mContext, CommentActivity.class);
        intent.putExtra(POST, post);
        mContext.startActivity(intent);
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

    private void sendNotification(Post post){
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String notificationID = reference.child("notifications").push().getKey();
            Notification notification = new Notification(FirebaseAuth.getInstance().getCurrentUser().getUid()
                    , post.getPublisher(), "like_post", " liked your post.",
                    getTimestamp(), post.getPostID(), post.getPostImage(), notificationID);
            reference.child("notifications").child(post.getPublisher()).child(notificationID).setValue(notification);
        }
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }


}
