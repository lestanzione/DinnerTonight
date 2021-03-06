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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class DinnerGroupActivity extends AppCompatActivity implements SuggestionRecyclerAdapter.SuggestionListener {

    private static final String TAG = DinnerGroupActivity.class.getSimpleName();

    private static final int CODE_NEW_DISH = 1;

    private Toolbar toolbar;
    private TextView dinnerGroupDateTitleTextView;
    private RecyclerView dinnerGroupSuggestionRecyclerView;

    private DinnerGroup dinnerGroup;
    private ArrayList<DinnerUser> dinnerUserList = new ArrayList<DinnerUser>();
    private ArrayList<String> creationUsernameList = new ArrayList<String>();
    private ArrayList<Suggestion> suggestionList = new ArrayList<Suggestion>();
    private ArrayList<Dish> dishList = new ArrayList<Dish>();

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dinnerGroupDateTitleTextView = (TextView) findViewById(R.id.dinnerGroupDateTitleTextView);
        dinnerGroupSuggestionRecyclerView = (RecyclerView) findViewById(R.id.dinnerGroupSuggestionRecyclerView);
        dinnerGroupSuggestionRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        dinnerGroupDateTitleTextView.setText(Util.getCurrentDateLabel());

        setSupportActionBar(toolbar);

        dinnerGroup = (DinnerGroup) getIntent().getSerializableExtra("SELECTED_DINNER_GROUP");

        Log.d(TAG, "DinnerGroup name:" + dinnerGroup.getName());
        getSupportActionBar().setTitle(dinnerGroup.getName());

        List<String> dinnerGroupMembers = dinnerGroup.getMembers();

        for(int i=0; i<dinnerGroupMembers.size(); i++){
            Log.d(TAG, dinnerGroupMembers.get(i));
            getUserInformation(dinnerGroupMembers.get(i));
        }

        getDaySuggestions();

    }

    private void showLoginActivity(){
        LoginActivity.start(getApplicationContext());
    }

    private void showDishesActivity(){
        DishesActivity.start(getApplicationContext(), dinnerGroup);
    }

    private void showDinnerGroupMembersActivity(){
        DinnerGroupMembersActivity.start(this, dinnerGroup);
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

        String currentDate = Util.getCurrentDate(); //"20160926";

        mDatabase.child(Configs.NODE_DAYS).child(dinnerGroup.getId()).child(currentDate).child(Configs.NODE_SUGGESTIONS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "Suggestions count: " + dataSnapshot.getChildrenCount());
                        Iterable<DataSnapshot> allSuggestions = dataSnapshot.getChildren();
                        while(allSuggestions.iterator().hasNext()){

                            Suggestion suggestion = allSuggestions.iterator().next().getValue(Suggestion.class);

                            ArrayList<String> voteUsersList = suggestion.getVoteUsers();
                            Log.d(TAG, "voteUsersList: " + voteUsersList);

                            String dishId = suggestion.getDishId();
                            Log.d(TAG, "dishId: " + dishId);

                            suggestionList.add(suggestion);

                            getDish(dishId);

                        }

                        showSuggestions();

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

        mDatabase.child(Configs.NODE_DISHES).child(dishId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Dish dish = dataSnapshot.getValue(Dish.class);
                        if(dish == null){
                            Log.d(TAG, "Could not find dish for this ID");
                        }
                        else {
                            Log.d(TAG, "Dish name: " + dish.getName());
                            Log.d(TAG, "Dish creation user ID: " + dish.getCreationUserId());
                            dishList.add(dish);
                            getCreationUsername(dish.getCreationUserId());
                            updateSuggestionRecyclerView();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getDish:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void getCreationUsername(String userId){

        boolean userFound = false;

        for(int i=0; i<dinnerUserList.size(); i++){
            //Log.d(TAG, "is " + dinnerUserList.get(i).getUid() + " == " + userId + " ?");
            if(userId.equalsIgnoreCase(dinnerUserList.get(i).getUid())){
                //Log.d(TAG, "found user: " + dinnerUserList.get(i).getDisplayName());
                creationUsernameList.add(dinnerUserList.get(i).getDisplayName());
                userFound = true;
                break;
            }
        }

        if(!userFound) {
            creationUsernameList.add("");
        }

    }

    private void showSuggestions(){

        Log.d(TAG, "inside showSuggestions() method with " + dishList.size() + " suggestions");

        SuggestionRecyclerAdapter suggestionRecyclerAdapter = new SuggestionRecyclerAdapter(getApplicationContext(), dishList, suggestionList, creationUsernameList, this);
        dinnerGroupSuggestionRecyclerView.setAdapter(suggestionRecyclerAdapter);

    }

    private void updateSuggestionRecyclerView(){

        Log.d(TAG, "inside updateSuggestionRecyclerView() method with " + dishList.size() + " suggestions");
        Log.d(TAG, "inside updateSuggestionRecyclerView() method with " + creationUsernameList.size() + " users");

        //for(String n : creationUsernameList){
        //    Log.d(TAG, "name: " + n);
        //}

        dinnerGroupSuggestionRecyclerView.getAdapter().notifyDataSetChanged();

    }

    private void addSuggestion(Dish selectedDish){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentDate = Util.getCurrentDate(); //"20160926";

        String newSuggestionKey = mDatabase.child(Configs.NODE_DAYS).child(dinnerGroup.getId()).child(currentDate).child(Configs.NODE_SUGGESTIONS).push().getKey();

        Suggestion newSuggestion = new Suggestion();
        newSuggestion.setId(newSuggestionKey);
        newSuggestion.setDay(currentDate);
        newSuggestion.setCreationUserId(user.getUid());
        newSuggestion.setCreationTimestamp(Util.getTimestamp());
        newSuggestion.setActive(true);
        newSuggestion.setDishId(selectedDish.getId());

        mDatabase.child(Configs.NODE_DAYS).child(dinnerGroup.getId()).child(currentDate).child(Configs.NODE_SUGGESTIONS).child(newSuggestionKey).setValue(newSuggestion);

        Toast.makeText(DinnerGroupActivity.this, "Suggestion added successfully!", Toast.LENGTH_SHORT).show();

    }

    private void createDish(){
        Intent createDishIntent = new Intent(getApplicationContext(), NewDishActivity.class);
        startActivityForResult(createDishIntent, CODE_NEW_DISH);
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_NEW_DISH){

            if(resultCode == RESULT_OK){
                Toast.makeText(DinnerGroupActivity.this, "Dish created successfully!", Toast.LENGTH_SHORT).show();
                Dish createdDish = (Dish) data.getSerializableExtra("CREATED_DISH");
                Log.d(TAG, "Created dish ID: " + createdDish.getId());

                addSuggestion(createdDish);

            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(DinnerGroupActivity.this, "Error creating dish", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public void onSuggestionSelected(int position) {

    }

    @Override
    public void onSuggestionVote(int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentDate = Util.getCurrentDate(); //"20160926";

        Suggestion selectedSuggestion = suggestionList.get(position);

        ArrayList<String> voteUsersList = selectedSuggestion.getVoteUsers();

        if(voteUsersList.contains(user.getUid())){
            //downvote
            selectedSuggestion.removeVoteUser(user.getUid());
            selectedSuggestion.removeVote();
        }
        else {
            //upvote
            selectedSuggestion.addVoteUser(user.getUid());
            selectedSuggestion.addVote();
        }

        mDatabase.child(Configs.NODE_DAYS).child(dinnerGroup.getId()).child(currentDate).child(Configs.NODE_SUGGESTIONS).child(selectedSuggestion.getId()).setValue(selectedSuggestion);

        Toast.makeText(DinnerGroupActivity.this, "Suggestion upvoted successfully!", Toast.LENGTH_SHORT).show();

        updateSuggestionRecyclerView();

    }

    public static void start(Context context, DinnerGroup dinnerGroup) {
        Intent starter = new Intent(context, DinnerGroupActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra("SELECTED_DINNER_GROUP", dinnerGroup);
        context.startActivity(starter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dinner_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.menuItemDinnerGroupAddSuggestion:
                showDishesActivity();
                return true;
            case R.id.menuItemDinnerGroupCreateDish:
                createDish();
                return true;
            case R.id.menuItemDinnerGroupMembers:
                showDinnerGroupMembersActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

}
