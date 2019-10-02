package com.seemantshekhar.befikre;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.seemantshekhar.befikre.Model.Post;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.Model.UserAccountSettings;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    int imageCount;

    ImageView postImage;
    ImageButton closeBtn;
    TextView postBtn;
    EditText postDesc;

    CircleImageView profileImage;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        postImage = (ImageView) findViewById(R.id.post_image_add_post);
        closeBtn = (ImageButton) findViewById(R.id.close_btn_add_post);
        postBtn = (TextView) findViewById(R.id.post_btn_add_post);
        postDesc = (EditText) findViewById(R.id.post_desc_add_post);
        profileImage = (CircleImageView) findViewById(R.id.image_profile_add_post);
        username = (TextView) findViewById(R.id.username_add_post);

        storageReference = FirebaseStorage.getInstance().getReference("posts");
        userInfo();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageCount = getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: imageCount " + imageCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = PostActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                uploadImage();
            }
        });

        CropImage.activity()
                .start(PostActivity.this);

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.setCanceledOnTouchOutside(false);


        if(imageUri != null){
            progressDialog.show();

            Log.d(TAG, "uploadImage: imagecount" +  (imageCount+1));
            final StorageReference fileReference = storageReference
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("photo" + (imageCount + 1));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        String postID = reference.child("posts").push().getKey();
                        String publisher = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Post post = new Post(postID, publisher, postDesc.getText().toString(), myUrl, getTimestamp());
                        reference.child("posts").child(postID).setValue(post);
                        reference.child("user_posts").child(publisher).child(postID).setValue(post);
                        //reference.child("users_account_settings").child(publisher).child("posts").setValue(imageCount+1);
                        progressDialog.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Error Uploading Post", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(PostActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            postImage.setImageURI(imageUri);
        } else {
            Toast.makeText(this ,"Error loading image", Toast.LENGTH_SHORT).show();
            onBackPressed();
            finish();
        }
    }

    private int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot.child("user_posts")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .getChildren()){
            count++;
        }
        Log.d(TAG, "getImageCount: " + count);
        return count;
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getContext() == null){
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                Glide.with(PostActivity.this).load(user.getProfile_image()).into(profileImage);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
