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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.seemantshekhar.befikre.Fragment.SearchFragment;
import com.seemantshekhar.befikre.Model.UserAccountSettings;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    private static final String TAG = "EditProfile";

    private UserAccountSettings settings;
    private String activityNumber;
    private CircleImageView imageProfile;
    private TextView changePic;
    private EditText username;
    private EditText fullName;
    private EditText bio;
    private ImageButton backBtn;
    private TextView save;
    private Button logOut;
    private Uri imageUri;
    private String myUrl = "";

    private StorageReference storageReference;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getIntent().getExtras() != null) {
            settings = getIntent().getExtras().getParcelable("userSetting");
            activityNumber = getIntent().getExtras().getString("activityNumber");
            Log.d(TAG, "onCreate: settings: " + settings);
            Log.d(TAG, "onCreate: activity no: " + activityNumber);
        }
        imageProfile = (CircleImageView) findViewById(R.id.profile_image_edit_profile);
        changePic = (TextView) findViewById(R.id.change_profile_pic_edit_profile);
        username = (EditText) findViewById(R.id.username_edit_text_edit_profile);
        fullName = (EditText) findViewById(R.id.full_name_edit_text_edit_profile);
        bio = (EditText) findViewById(R.id.bio_edit_text_edit_profile);
        backBtn = (ImageButton) findViewById(R.id.back_btn_edit_profile);
        save = (TextView) findViewById(R.id.save_btn_edit_profile);
        logOut = (Button) findViewById(R.id.log_out_btn);
        storageReference = FirebaseStorage.getInstance().getReference();

        if(activityNumber.equals("0")){
            backBtn.setVisibility(View.GONE);
            logOut.setVisibility(View.GONE);
        }

        Glide.with(this).load(settings.getProfile_image()).into(imageProfile);
        username.setText(settings.getUsername());
        fullName.setText(settings.getFull_name());
        bio.setText(settings.getBio());

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditProfile.this);
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditProfile.this);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = EditProfile.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (TextUtils.isEmpty(fullName.getText().toString()) || TextUtils.isEmpty(username.getText().toString())) {
                    Toast.makeText(EditProfile.this, "Username and Full Name fields required!", Toast.LENGTH_SHORT).show();
                } else if(username.getText().toString().equals(settings.getUsername())){
                    updateProfile(fullName.getText().toString(), bio.getText().toString());
                } else if(username.getText().toString().equals(settings.getUsername()) &&
                fullName.getText().toString().equals(settings.getFull_name()) &&
                        bio.getText().toString().equals(settings.getBio())){
                    if(activityNumber.equals("1")){
                        onBackPressed();
                    } else if(activityNumber.equals("0")){
                        searchFragment();
                    }

                } else {
                    updateProfile(fullName.getText().toString(),username.getText().toString(),bio.getText().toString());
                }

            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users_account_settings")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.child("device_token").removeValue();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EditProfile.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });


    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating Profile Pic");
        pd.setCanceledOnTouchOutside(false);


        if (imageUri != null) {
            pd.show();
            final StorageReference fileReference = storageReference
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("profile_photo");
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("profile_image", "" + myUrl);

                        reference.child("users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(hashMap);
                        reference.child("users_account_settings")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(hashMap);

                        pd.dismiss();

                    } else {
                        pd.dismiss();
                        Toast.makeText(EditProfile.this, "Error Uploading Post", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(EditProfile.this, "Error Updating Profile Pic", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            imageProfile.setImageURI(imageUri);
            uploadImage();
        } else {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateProfile(final String fullName, final String bio){
        final ProgressDialog pd = new ProgressDialog(EditProfile.this);
        pd.setMessage("Updating info");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("full_name", fullName);
        hashMap.put("bio", bio);

        reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(hashMap);
        reference.child("users_account_settings").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(hashMap);
        pd.dismiss();
        if(activityNumber.equals("1")){
            onBackPressed();
        } else if(activityNumber.equals("0")){
            searchFragment();
        }
    }

    private void updateProfile(final String fullName, final String username, final String bio) {
        final ProgressDialog pd = new ProgressDialog(EditProfile.this);
        pd.setMessage("Updating info");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = FirebaseDatabase.getInstance()
                .getReference().child("users")
                .orderByChild("username")
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int result = 1;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        result = 0;
                        Toast.makeText(EditProfile.this, "Username already exists!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }

                if (result == 1) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("full_name", fullName);
                    hashMap.put("username", username);
                    hashMap.put("bio", bio);

                    reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(hashMap);
                    reference.child("users_account_settings").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(hashMap);
                    pd.dismiss();
                    if(activityNumber.equals("1")){
                        onBackPressed();
                    } else if(activityNumber.equals("0")){
                        searchFragment();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void searchFragment(){
        Intent intent=  new Intent(EditProfile.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}