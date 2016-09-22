package com.empire.android.dinnertonight;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DinnerGroupActivity extends AppCompatActivity {

    private static final String TAG = DinnerGroupActivity.class.getSimpleName();

    private static final int CODE_NEW_DISH = 1;

    private Button dinnerGroupAddMemberButton;
    private Button dinnerGroupAddSuggestionButton;
    private Button dinnerGroupAddDishButton;

    private DinnerGroup dinnerGroup;
    private ArrayList<DinnerUser> dinnerUserList = new ArrayList<DinnerUser>();

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_group);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        dinnerGroupAddMemberButton = (Button) findViewById(R.id.dinnerGroupAddMemberButton);
        dinnerGroupAddSuggestionButton = (Button) findViewById(R.id.dinnerGroupAddSuggestionButton);
        dinnerGroupAddDishButton = (Button) findViewById(R.id.dinnerGroupAddDishButton);

        dinnerGroupAddMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });

        dinnerGroupAddSuggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSuggestion("123");
            }
        });

        dinnerGroupAddDishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDish();
            }
        });

        dinnerGroup = (DinnerGroup) getIntent().getSerializableExtra("SELECTED_DINNER_GROUP");

        Log.d(TAG, "DinnerGroup name:" + dinnerGroup.getName());

        List<String> dinnerGroupMembers = dinnerGroup.getMembers();

        for(int i=0; i<dinnerGroupMembers.size(); i++){
            Log.d(TAG, dinnerGroupMembers.get(i));
            getUserInformation(dinnerGroupMembers.get(i));
        }

        getDaySuggestions();

    }

    private void showLoginActivity(){

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void getUserInformation(String userId){

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DinnerUser dinnerGroupMemberUser = dataSnapshot.getValue(DinnerUser.class);

                        Log.d(TAG, "User in group: " + dinnerGroupMemberUser.getDisplayName());

                        dinnerUserList.add(dinnerGroupMemberUser);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUserInformation:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void getDaySuggestions(){

        Log.d(TAG, "Getting today suggestions");

        mDatabase.child("days").child(dinnerGroup.getId()).child("20160926").child("suggestions").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "Suggestions count: " + dataSnapshot.getChildrenCount());
                        Iterable<DataSnapshot> allSuggestions = dataSnapshot.getChildren();
                        while(allSuggestions.iterator().hasNext()){

                            DaySuggestion daySuggestion = allSuggestions.iterator().next().getValue(DaySuggestion.class);
                            String dishId = daySuggestion.getDishId();
                            Log.d(TAG, "dishId: " + dishId);

                            getDish(dishId);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getDaySuggestions:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void getDish(String dishId){

        Log.d(TAG, "Searching for dish with id: " + dishId);

        mDatabase.child("dishes").child(dishId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Dish dish = dataSnapshot.getValue(Dish.class);
                        if(dish == null){
                            Log.d(TAG, "Could not find dish for this ID");
                        }
                        else {
                            Log.d(TAG, "Dish name: " + dish.getName());
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getDish:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void addSuggestion(String dishId){

        String newSuggestionKey = mDatabase.child("days").child(dinnerGroup.getId()).child("20160926").child("suggestions").push().getKey();
        mDatabase.child("days").child(dinnerGroup.getId()).child("20160926").child("suggestions").child(newSuggestionKey).child("dishId").setValue(dishId);

        Toast.makeText(DinnerGroupActivity.this, "Suggestion added successfully!", Toast.LENGTH_SHORT).show();

    }

    private void createDish(){
        Intent createDishIntent = new Intent(getApplicationContext(), CreateDishActivity.class);
        startActivityForResult(createDishIntent, CODE_NEW_DISH);
    };

    private void showAddUserDialog(){
        new AddUserDialogFragment().show(getSupportFragmentManager(), null);
    }

    private void checkUser(final String userEmail){

        mDatabase.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "Users count: " + dataSnapshot.getChildrenCount());
                        Iterable<DataSnapshot> allUsers = dataSnapshot.getChildren();
                        while(allUsers.iterator().hasNext()){

//                            DataSnapshot currentUser = allUsers.iterator().next();
//                            String email = currentUser.child("email").getValue().toString();
//                            Log.d(TAG, "currentUser email: " + email);

                            DinnerUser dinnerUser = allUsers.iterator().next().getValue(DinnerUser.class);
                            String email = dinnerUser.getEmail();
                            Log.d(TAG, "email: " + email);

                            if(email.equalsIgnoreCase(userEmail)){
                                String uid = dinnerUser.getUid();
                                //String uid = currentUser.child("uid").getValue().toString();
                                Log.d(TAG, "Found user " + uid + " with email: " + userEmail);

                                addUser(uid);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "checkUser:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void addUser(String uid){

        dinnerGroup.getMembers().add(uid);

        Log.d(TAG, "/dinnerGroups/" + dinnerGroup.getId() + "/members");
        Log.d(TAG, "/user-dinnerGroups/" + uid + "/" + dinnerGroup.getId());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/dinnerGroups/" + dinnerGroup.getId() + "/members", dinnerGroup.getMembers());
        childUpdates.put("/user-dinnerGroups/" + uid + "/" + dinnerGroup.getId(), true);

        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Log.d(TAG, "Added successfully!");

                }
                else{

                    Log.d(TAG, "Error adding user to group: " + databaseError.getMessage());

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_NEW_DISH){

            if(resultCode == RESULT_OK){
                Toast.makeText(DinnerGroupActivity.this, "Dish created successfully!", Toast.LENGTH_SHORT).show();
                Dish createdDish = (Dish) data.getSerializableExtra("CREATED_DISH");
                Log.d(TAG, "Created dish ID: " + createdDish.getId());

                addSuggestion(createdDish.getId());

            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(DinnerGroupActivity.this, "Error creating dish", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @SuppressLint("ValidFragment")
    public class AddUserDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.dialog_add_user, null);

            final EditText addUserToGroupEditText = (EditText) view.findViewById(R.id.addUserToGroupEditText);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Add user to group")
                    .setView(view)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {


                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            final AlertDialog alertDialog = builder.create();

            //used to override the positive button event
            //that way is possible to prevent the dialog dismiss if the input is wrong
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            String userEmail = addUserToGroupEditText.getText().toString().trim();
                            checkUser(userEmail);

                            if (userEmail.isEmpty()) {


                                //Snackbar.make(itemsCoordinatorLayout, getResources().getString(R.string.party_saved_error), Snackbar.LENGTH_SHORT).show();
                                //new NoNamePartyDialogFragment().show(getSupportFragmentManager(), null);
                            } else {
                                //saveList(partyName);
                                alertDialog.dismiss();
                            }

                        }
                    });

                }
            });



            return alertDialog;
        }

    }

}
