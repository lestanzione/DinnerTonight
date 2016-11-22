package com.empire.android.dinnertonight;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class DinnerGroupMembersActivity extends AppCompatActivity implements DinnerGroupMemberRecyclerAdapter.DinnerGroupMemberListener {

    private static final String TAG = DinnerGroupMembersActivity.class.getSimpleName();

    private Toolbar toolbar;
    private RecyclerView dinnerGroupMemberRecyclerView;

    private DinnerGroup dinnerGroup;
    private ArrayList<DinnerUser> dinnerUserList = new ArrayList<DinnerUser>();

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_group_members);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dinnerGroupMemberRecyclerView = (RecyclerView) findViewById(R.id.dinnerGroupMemberRecyclerView);
        dinnerGroupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        setSupportActionBar(toolbar);

        dinnerGroup = (DinnerGroup) getIntent().getSerializableExtra("SELECTED_DINNER_GROUP");

        Log.d(TAG, "DinnerGroup name:" + dinnerGroup.getName());
        getSupportActionBar().setTitle(dinnerGroup.getName());

        List<String> dinnerGroupMembers = dinnerGroup.getMembers();
        showMembers(dinnerGroupMembers);

    }

    private void showLoginActivity(){
        LoginActivity.start(getApplicationContext());
    }

    private void getUserInformation(String userId){

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DinnerUser dinnerGroupMemberUser = dataSnapshot.getValue(DinnerUser.class);

                        Log.d(TAG, "User in group: " + dinnerGroupMemberUser.getDisplayName());

                        dinnerUserList.add(dinnerGroupMemberUser);
                        updateMembersRecyclerView();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUserInformation:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void clearMembers(){

        dinnerUserList = new ArrayList<>();
        dinnerGroupMemberRecyclerView.getAdapter().notifyDataSetChanged();

    }

    private void showMembers(List<String> dinnerGroupMembers){

        Log.d(TAG, "inside showMembers() method with " + dinnerUserList.size() + " members");

        DinnerGroupMemberRecyclerAdapter dinnerGroupMemberRecyclerAdapter = new DinnerGroupMemberRecyclerAdapter(getApplicationContext(), dinnerUserList, this);
        dinnerGroupMemberRecyclerView.setAdapter(dinnerGroupMemberRecyclerAdapter);

        for(int i=0; i<dinnerGroupMembers.size(); i++){
            Log.d(TAG, dinnerGroupMembers.get(i));
            getUserInformation(dinnerGroupMembers.get(i));
        }

    }

    private void updateMembersRecyclerView(){

        Log.d(TAG, "inside updateMembersRecyclerView() method with " + dinnerUserList.size() + " members");

        dinnerGroupMemberRecyclerView.getAdapter().notifyDataSetChanged();

    }

    private void showAddUserDialog(){
        new AddUserDialogFragment().show(getSupportFragmentManager(), null);
    }

    private void checkUser(final String userEmail){

        mDatabase.child(Configs.NODE_USERS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        
                        String foundUid = null;

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

                                foundUid = uid;

                            }

                        }
                        
                        if(foundUid != null){

                            if(dinnerGroup.getMembers().contains(foundUid)){
                                Toast.makeText(DinnerGroupMembersActivity.this, userEmail + " is already a member of this group.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                addUser(foundUid);
                            }
                            
                        }
                        else{
                            Toast.makeText(DinnerGroupMembersActivity.this, "Could not find any user with this email. Please, try again.", Toast.LENGTH_LONG).show();
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

        Log.d(TAG, "/" + Configs.NODE_DINNER_GROUPS + "/" + dinnerGroup.getId() + "/" + Configs.NODE_MEMBERS);
        Log.d(TAG, "/user-dinnerGroups/" + uid + "/" + dinnerGroup.getId());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Configs.NODE_DINNER_GROUPS + "/" + dinnerGroup.getId() + "/" + Configs.NODE_MEMBERS, dinnerGroup.getMembers());
        childUpdates.put("/" + Configs.NODE_USER_DINNER_GROUPS + "/" + uid + "/" + dinnerGroup.getId(), true);

        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Log.d(TAG, "Added successfully!");
                    clearMembers();
                    List<String> dinnerGroupMembers = dinnerGroup.getMembers();
                    showMembers(dinnerGroupMembers);

                }
                else{

                    Log.d(TAG, "Error adding user to group: " + databaseError.getMessage());

                }

            }
        });

    }

    private void removeUser(String uid){

        dinnerGroup.getMembers().remove(uid);

        Log.d(TAG, "/" + Configs.NODE_DINNER_GROUPS + "/" + dinnerGroup.getId() + "/" + Configs.NODE_MEMBERS);
        Log.d(TAG, "/user-dinnerGroups/" + uid + "/" + dinnerGroup.getId());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Configs.NODE_DINNER_GROUPS + "/" + dinnerGroup.getId() + "/" + Configs.NODE_MEMBERS, dinnerGroup.getMembers());
        childUpdates.put("/" + Configs.NODE_USER_DINNER_GROUPS + "/" + uid + "/" + dinnerGroup.getId(), null); //setting null removes the node from the database

        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Log.d(TAG, "Removed successfully!");
                    clearMembers();
                    List<String> dinnerGroupMembers = dinnerGroup.getMembers();
                    showMembers(dinnerGroupMembers);

                }
                else{

                    Log.d(TAG, "Error removing user to group: " + databaseError.getMessage());

                }

            }
        });

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
                            //checkUser(userEmail);

                            if (userEmail.isEmpty()) {
                                //Snackbar.make(itemsCoordinatorLayout, getResources().getString(R.string.party_saved_error), Snackbar.LENGTH_SHORT).show();
                                //new NoNamePartyDialogFragment().show(getSupportFragmentManager(), null);
                            } else {
                                checkUser(userEmail);
                                alertDialog.dismiss();
                            }

                        }
                    });

                }
            });

            return alertDialog;
        }

    }

    public static void start(Context context, DinnerGroup dinnerGroup) {
        Intent starter = new Intent(context, DinnerGroupMembersActivity.class);
        starter.putExtra("SELECTED_DINNER_GROUP", dinnerGroup);
        context.startActivity(starter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dinner_group_members, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.menuItemDinnerGroupMemberAddMember:
                showAddUserDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onDinnerGroupMemberSelected(int position) {

    }

    @Override
    public void onRemoveDinnerGroupMember(int position) {

        DinnerUser userToRemove = dinnerUserList.get(position);
        Log.d(TAG, "Removing user: " + userToRemove.getUid() + " - " + userToRemove.getDisplayName());
        removeUser(userToRemove.getUid());

    }
}
