package com.seemantshekhar.befikre;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LogInActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    TextView signUp;
    TextView forgotPassword;
    Button logIn;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        email = (EditText) findViewById(R.id.email_edit_text_log_in);
        password = (EditText) findViewById(R.id.password_edit_text_log_in);
        signUp = (TextView) findViewById(R.id.sign_up_text_view);
        forgotPassword = (TextView) findViewById(R.id.forgot_password_text_view);
        logIn = (Button) findViewById(R.id.log_in_btn_log_in_activity);

        auth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LogInActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                final ProgressDialog pd = new ProgressDialog(LogInActivity.this);
                pd.setMessage("Logging In");
                pd.setCanceledOnTouchOutside(false);

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if(TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                    Toast.makeText(LogInActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if(!str_email.matches(SignUpActivity.EMAIL_PATTERN)) {
                    Toast.makeText(LogInActivity.this, "Enter correct Email", Toast.LENGTH_SHORT).show();
                } else {
                    pd.show();
                    auth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        String token = FirebaseInstanceId.getInstance().getToken();
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users_account_settings")
                                                .child(auth.getCurrentUser().getUid());
                                        reference.child("device_token").setValue(token);
                                        pd.dismiss();

                                        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.hide();
                            if( e instanceof FirebaseAuthInvalidUserException){
                                Toast.makeText(LogInActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            if( e instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(LogInActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            }
                            if(e instanceof FirebaseNetworkException){
                                Toast.makeText(LogInActivity.this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String str_email = email.getText().toString();
                if(TextUtils.isEmpty(str_email)){
                    Toast.makeText(LogInActivity.this, "Enter Registered Email", Toast.LENGTH_SHORT).show();
                } else if (!str_email.matches(SignUpActivity.EMAIL_PATTERN)){
                    Toast.makeText(LogInActivity.this, "Enter Correct Email", Toast.LENGTH_SHORT).show();
                } else {

                            auth.sendPasswordResetEmail(str_email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(LogInActivity.this,"Reset Password link has been sent to " + str_email, Toast.LENGTH_SHORT).show();

                                            }
                                        }
                    });
                }
            }
        });
    }
}
