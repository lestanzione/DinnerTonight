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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DishesActivity extends AppCompatActivity implements DishRecyclerAdapter.DishListener {

    private static final String TAG = DishesActivity.class.getSimpleName();

    private DinnerGroup dinnerGroup;
    private Dish selectedDish;

    private Toolbar toolbar;
    private RecyclerView dishesRecyclerView;

    private ArrayList<Dish> dishList = new ArrayList<Dish>();

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        dinnerGroup = (DinnerGroup) getIntent().getSerializableExtra("SELECTED_DINNER_GROUP");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dishesRecyclerView = (RecyclerView) findViewById(R.id.dishesRecyclerView);
        dishesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dishes");

        getUserDishes(user.getUid());

    }

    private void showLoginActivity(){
        LoginActivity.start(getApplicationContext());
    }

    private void getUserDishes(String userId){

        Log.d(TAG, "Getting user dishes");

        mDatabase.child("user-dishes").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "Dishes count: " + dataSnapshot.getChildrenCount());
                        Iterable<DataSnapshot> allDishes = dataSnapshot.getChildren();
                        while(allDishes.iterator().hasNext()){

                            String dishId = String.valueOf(allDishes.iterator().next().getKey());
                            Log.d(TAG, "dishId: " + dishId);

                            getDish(dishId);

                        }

                        showDishes();

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
                            Log.d(TAG, "Dish creation user ID: " + dish.getCreationUserId());
                            dishList.add(dish);
                            //getCreationUsername(dish.getCreationUserId());
                            updateDishRecyclerView();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getDish:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    private void showDishes(){

        Log.d(TAG, "inside showDishes() method with " + dishList.size() + " dishes");

        DishRecyclerAdapter dishRecyclerAdapter = new DishRecyclerAdapter(getApplicationContext(), dishList, this);
        dishesRecyclerView.setAdapter(dishRecyclerAdapter);

    }

    private void updateDishRecyclerView(){

        Log.d(TAG, "inside updateDishRecyclerView() method with " + dishList.size() + " dishes");

        //for(String n : creationUsernameList){
        //    Log.d(TAG, "name: " + n);
        //}

        dishesRecyclerView.getAdapter().notifyDataSetChanged();

    }

    private void addSuggestion(Dish selectedDish){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentDay = "20160926";

        String newSuggestionKey = mDatabase.child("days").child(dinnerGroup.getId()).child(currentDay).child("suggestions").push().getKey();

        Suggestion newSuggestion = new Suggestion();
        newSuggestion.setId(newSuggestionKey);
        newSuggestion.setDay(currentDay);
        newSuggestion.setCreationUserId(user.getUid());
        newSuggestion.setCreationTimestamp(Util.getTimestamp());
        newSuggestion.setActive(true);
        newSuggestion.setDishId(selectedDish.getId());

        mDatabase.child("days").child(dinnerGroup.getId()).child(currentDay).child("suggestions").child(newSuggestionKey).setValue(newSuggestion);

        Toast.makeText(DishesActivity.this, "Suggestion added successfully!", Toast.LENGTH_SHORT).show();

        finish();

    }

    @Override
    public void onDishSelected(int position) {

        selectedDish = dishList.get(position);
        new ConfirmDishDialogFragment().show(getSupportFragmentManager(), null);

    }

    @SuppressLint("ValidFragment")
    public class ConfirmDishDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Do you want to add this dish as a suggestion?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            addSuggestion(selectedDish);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            return builder.create();
        }

    }

    public static void start(Context context, DinnerGroup dinnerGroup) {
        Intent starter = new Intent(context, DishesActivity.class);
        starter.putExtra("SELECTED_DINNER_GROUP", dinnerGroup);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

}
