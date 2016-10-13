package com.empire.android.dinnertonight;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private int OPERATION_CODE = 0;
    private static final int CODE_LOGIN = 1;
    private static final int CODE_SIGNUP = 2;

    private Toolbar toolbar;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    Uri photoUrl = user.getPhotoUrl();

                    // The user's ID, unique to the Firebase project. Do NOT use this value to
                    // authenticate with your backend server, if you have one. Use
                    // FirebaseUser.getToken() instead.
                    final String uid = user.getUid();

                    Log.d(TAG, "name: " + name);
                    Log.d(TAG, "email: " + email);
                    Log.d(TAG, "photoUrl: " + photoUrl);
                    Log.d(TAG, "uid: " + uid);


                    if(OPERATION_CODE == CODE_SIGNUP){
                        Log.d(TAG, "Registering new user..");

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(email)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");

                                    mDatabase.child(Configs.NODE_USERS).child(uid).setValue(user);
                                }
                            }
                        });

                    }

                    showMainActivity();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });
        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        findViewById(R.id.loginAnonymousButton).setVisibility(View.INVISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void doSignUp(){

        OPERATION_CODE = CODE_SIGNUP;

        String email = ((EditText) findViewById(R.id.loginEmailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.loginPasswordEditText)).getText().toString();

        if(!validateFields(email, password)){
            Toast.makeText(LoginActivity.this, "Email or password incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });

//        Intent intent = this.getIntent();
//        intent.putExtra("SIGN_UP_EMAIL", email);
//        intent.putExtra("SIGN_UP_PASSWORD", password);
//        setResult(RESULT_OK, intent);
//        finish();

    }

    public void doLogin(){

        OPERATION_CODE = CODE_LOGIN;

        setProgressBarIndeterminate(true);

        String email = ((EditText) findViewById(R.id.loginEmailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.loginPasswordEditText)).getText().toString();

        if(!validateFields(email, password)){
            Toast.makeText(LoginActivity.this, "Email or password incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                });

    }

    private boolean validateFields(String email, String password){

        if(email.trim().isEmpty()){
            return false;
        }

        if(password.trim().isEmpty()){
            return false;
        }

        return true;
    }

    public void showMainActivity(){
        MainActivity.start(getApplicationContext());
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(starter);
    }

}
