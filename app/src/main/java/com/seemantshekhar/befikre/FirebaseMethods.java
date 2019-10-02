package com.seemantshekhar.befikre;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.Model.UserAccountSettings;

import java.io.FileInputStream;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private boolean result;

    private Context mContext;

    public FirebaseMethods(Context mContext) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        this.mContext = mContext;
        this.result = false;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void addNewUser(String userID, String email, String username, String bio, String profileImage, String fullName){
        Log.d(TAG, "addNewUser: started");
        User user = new User(userID, username, profileImage, email);
        myRef.child("users")
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(bio, fullName, 0, username, profileImage, email, "");
        myRef.child("users_account_settings")
                .child(userID)
                .setValue(settings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            returnResultTrue();
                            Log.d(TAG, "onComplete: success");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        returResultFalse();
                    }
                });
        Log.d(TAG, "addNewUser: result " + getResult());
        Log.d(TAG, "addNewUser: ended");

    }

    private void returnResultTrue(){
        this.result = true;
    }

    private void returResultFalse(){
        this.result = false;
    }

    public boolean getResult(){
        return result;
    }

}
