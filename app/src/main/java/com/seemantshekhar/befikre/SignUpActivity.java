package com.seemantshekhar.befikre;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.Model.UserAccountSettings;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    public static final int PASSWORD_LENGTH = 8;
    private static final String ACTIVITY_NUMBER = "activityNumber";
    public static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String TAG = "SignUpActivity";
    EditText fullName, email, password, confirmPassword;
    Button signUp;
    TextView logIn;

    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseMethods firebaseMethods;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fullName = (EditText) findViewById(R.id.full_name_edit_text);
        email = (EditText) findViewById(R.id.email_edit_text_sign_up);
        password = (EditText) findViewById(R.id.password_edit_text_sign_up);
        confirmPassword = (EditText) findViewById(R.id.confirm_password_edit_text_sign_up);
        logIn = (TextView) findViewById(R.id.log_in_text_view);
        signUp = (Button) findViewById(R.id.sign_up_btn_sign_up_activity);

        auth = FirebaseAuth.getInstance();
        firebaseMethods = new FirebaseMethods(this);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = SignUpActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                pd = new ProgressDialog(SignUpActivity.this);
                pd.setMessage("Signing Up");
                pd.setCanceledOnTouchOutside(false);


                String str_fullName  = fullName.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();
                String str_confirmPassword = confirmPassword.getText().toString();

                if(TextUtils.isEmpty(str_fullName) || TextUtils.isEmpty(str_password)
                        || TextUtils.isEmpty(str_confirmPassword) || TextUtils.isEmpty(str_email)){
                    Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if(str_password.length() < PASSWORD_LENGTH){
                    Toast.makeText(SignUpActivity.this,"Password must have 8 characters", Toast.LENGTH_SHORT).show();
                } else if(!str_password.equals(str_confirmPassword)){
                    Toast.makeText(SignUpActivity.this, "Password and Confirm Passowrd are different", Toast.LENGTH_SHORT).show();
                } else if(!str_email.matches(EMAIL_PATTERN)){
                    Toast.makeText(SignUpActivity.this, "Enter valid Email", Toast.LENGTH_SHORT).show();
                } else {
                    pd.show();
                    register(str_fullName,str_email,str_password);
                }
            }
        });
    }

    private void register(final String fullName, final String email, String password){

        Log.d(TAG, "register: starts");
        final String username = fullName.replaceAll(" ", "");
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){


                            reference = FirebaseDatabase.getInstance().getReference();
                            Query query = FirebaseDatabase.getInstance()
                                    .getReference().child("users")
                                    .orderByChild("username")
                                    .equalTo(username);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String newUsername = username;
                                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                                        if(singleSnapshot.exists()){
                                            DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();
                                            newUsername = username + databaseReference.push().getKey().substring(3,10);
                                        }
                                    }
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    String userId = firebaseUser.getUid();
                                    addNewUser(userId, email, newUsername, "Befikra bhi hona jaroori hai!", "https://firebasestorage.googleapis.com/v0/b/befikre-46530.appspot.com/o/user.png?alt=media&token=bd09b870-c6ac-486b-8462-bffaeb1631cd", fullName);

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.hide();

                if( e instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(SignUpActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                }

                if(e instanceof FirebaseAuthEmailException) {
                    Toast.makeText(SignUpActivity.this, "Enter correct Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.d(TAG, "register: ends");
    }

    public void addNewUser(String userID, String email, String username, String bio, String profileImage, String fullName){
        Log.d(TAG, "addNewUser: started");
        User user = new User(userID, username,fullName, profileImage);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("users")
                .child(userID)
                .setValue(user);
        String device_token = FirebaseInstanceId.getInstance().getToken();
        final UserAccountSettings settings = new UserAccountSettings(bio, fullName, 0, username, profileImage, email, device_token);
        myRef.child("users_account_settings")
                .child(userID)
                .setValue(settings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: success");
                            pd.dismiss();
                            Intent intent = new Intent(SignUpActivity.this, EditProfile.class);
                            intent.putExtra(ACTIVITY_NUMBER, "0");
                            intent.putExtra("userSetting", settings);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        Log.d(TAG, "addNewUser: ended");

    }
}
