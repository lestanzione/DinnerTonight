package com.empire.android.dinnertonight;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DinnerGroupRecyclerAdapter.DinnerGroupListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CODE_NEW_DINNER_GROUP = 1;

    private boolean signedUser = false;

    private Toolbar toolbar;
    private Button createDinnerGroupButton;
    private Button logoutButton;
    private RecyclerView dinnerGroupRecyclerView;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ArrayList<DinnerGroup> userDinnerGroupList = new ArrayList<DinnerGroup>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        createDinnerGroupButton = (Button) findViewById(R.id.createDinnerGroupButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        dinnerGroupRecyclerView = (RecyclerView) findViewById(R.id.dinnerGroupRecyclerView);
        dinnerGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dinner Tonight");

        createDinnerGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDinnerGroup();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if(signedUser){
                        return;
                    }

                    signedUser = true;

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    Uri photoUrl = user.getPhotoUrl();

                    // The user's ID, unique to the Firebase project. Do NOT use this value to
                    // authenticate with your backend server, if you have one. Use
                    // FirebaseUser.getToken() instead.
                    String uid = user.getUid();

                    Log.d(TAG, "name: " + name);
                    Log.d(TAG, "email: " + email);
                    Log.d(TAG, "photoUrl: " + photoUrl);
                    Log.d(TAG, "uid: " + uid);

                    initializeScreen();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    showLoginActivity();

                }
                // ...
            }
        };

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

    private void showLoginActivity(){
        LoginActivity.start(getApplicationContext());
    }

    private void signOut(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Toast.makeText(MainActivity.this, "You are not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signOut();

    }

    private void initializeScreen(){

        getGroups();

    }

    private void getGroups(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child(Configs.NODE_USER_DINNER_GROUPS).child(user.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "Groups count: " + dataSnapshot.getChildrenCount());

                        List<String> userDinnerGroupKeyList = new ArrayList<String>();

                        Iterable<DataSnapshot> dinnerGroupDataSnapshots = dataSnapshot.getChildren();
                        while(dinnerGroupDataSnapshots.iterator().hasNext()){
                            //Log.d(TAG, "NAME: " + petArrayList.iterator().next().child("name").getValue());

                            String dinnerGroupKey = dinnerGroupDataSnapshots.iterator().next().getKey();
                            Log.d(TAG, "Group ID: " + dinnerGroupKey);

                            userDinnerGroupKeyList.add(dinnerGroupKey);

                        }

                        for(int i=0; i<userDinnerGroupKeyList.size(); i++) {
                            getGroupsObject(userDinnerGroupKeyList.get(i));
                        }

                        showDinnerGroups();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getGroupsKey:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void getGroupsObject(final String dinnerGroupKey){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child(Configs.NODE_DINNER_GROUPS).child(dinnerGroupKey).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DinnerGroup foundDinnerGroup = dataSnapshot.getValue(DinnerGroup.class);
                        Log.d(TAG, "Group name: " + foundDinnerGroup.getName());
                        if(foundDinnerGroup.getId() == null) {
                            foundDinnerGroup.setId(dinnerGroupKey);
                        }

                        userDinnerGroupList.add(foundDinnerGroup);
                        updateDinnerGroupRecyclerView();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getGroupsObject:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void showDinnerGroups(){

        Log.d(TAG, "inside showDinnerGroups() method with " + userDinnerGroupList.size() + " groups");

        DinnerGroupRecyclerAdapter dinnerGroupRecyclerAdapter = new DinnerGroupRecyclerAdapter(getApplicationContext(), userDinnerGroupList, this);
        dinnerGroupRecyclerView.setAdapter(dinnerGroupRecyclerAdapter);

    }

    private void updateDinnerGroupRecyclerView(){

        Log.d(TAG, "inside updateDinnerGroupRecyclerView() method with " + userDinnerGroupList.size() + " groups");
        dinnerGroupRecyclerView.getAdapter().notifyDataSetChanged();

    }

    private void createDinnerGroup(){

        Intent createDinnerGroupIntent = new Intent(getApplicationContext(), NewDinnerGroupActivity.class);
        startActivityForResult(createDinnerGroupIntent, CODE_NEW_DINNER_GROUP);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_NEW_DINNER_GROUP){

            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this, "Group created successfully!", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == RESULT_CANCELED){

            }

        }

    }

    @Override
    public void onDinnerGroupSelected(int position){
        Toast.makeText(MainActivity.this, "Group selected: " + userDinnerGroupList.get(position).getName(), Toast.LENGTH_SHORT).show();

        DinnerGroupActivity.start(getApplicationContext(), userDinnerGroupList.get(position));

    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(starter);
    }

}
