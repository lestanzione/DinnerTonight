package com.empire.android.dinnertonight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewDinnerGroupActivity extends AppCompatActivity {

    private static final String TAG = NewDinnerGroupActivity.class.getSimpleName();

    private Toolbar toolbar;
    private EditText newDinnerGroupNameEditText;
    private Button newDinnerGroupCreateButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dinner_group);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        newDinnerGroupNameEditText = (EditText) findViewById(R.id.newDinnerGroupNameEditText);
        newDinnerGroupCreateButton = (Button) findViewById(R.id.newDinnerGroupCreateButton);

        newDinnerGroupCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Group");

    }

    private void showLoginActivity(){

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void createGroup(){

        String dinnerGroupName = newDinnerGroupNameEditText.getText().toString();

        if(dinnerGroupName.trim().isEmpty()){
            Toast.makeText(NewDinnerGroupActivity.this, "Give a name to your new group!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<String> membersList = new ArrayList<String>();
        membersList.add(user.getUid());

        String newDinnerGroupKey = mDatabase.child("dinnerGroups").push().getKey();

        DinnerGroup newDinnerGroup = new DinnerGroup();
        newDinnerGroup.setId(newDinnerGroupKey);
        newDinnerGroup.setName(dinnerGroupName);
        newDinnerGroup.setActive(true);
        newDinnerGroup.setCreationUserId(user.getUid());
        newDinnerGroup.setMembers(membersList);

        Map<String, Object> postValues = newDinnerGroup.toMap();

        Log.d(TAG, "/dinnerGroups/" + newDinnerGroupKey);
        Log.d(TAG, "/user-dinnerGroups/" + user.getUid() + "/" + newDinnerGroupKey);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/dinnerGroups/" + newDinnerGroupKey, postValues);
        childUpdates.put("/user-dinnerGroups/" + user.getUid() + "/" + newDinnerGroupKey, true);

        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    setResult(RESULT_OK);
                    finish();

                }
                else{

                    setResult(RESULT_CANCELED);
                    finish();

                }

            }
        });

    }

}
