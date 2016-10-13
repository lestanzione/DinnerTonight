package com.empire.android.dinnertonight;

import android.content.Context;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewDishActivity extends AppCompatActivity {

    private static final String TAG = NewDishActivity.class.getSimpleName();

    private Toolbar toolbar;
    private EditText newDishNameEditText;
    private Button newDishCreateButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dish);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        newDishNameEditText = (EditText) findViewById(R.id.newDishNameEditText);
        newDishCreateButton = (Button) findViewById(R.id.newDishCreateButton);

        newDishCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDish();
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Dish");

    }

    private void showLoginActivity(){
        LoginActivity.start(getApplicationContext());
    }

    private void createDish(){

        String dishName = newDishNameEditText.getText().toString().trim();

        if(dishName.trim().isEmpty()){
            Toast.makeText(NewDishActivity.this, "Give a name to your new dish!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String newDishKey = mDatabase.child("dishes").push().getKey();

        final Dish newDish = new Dish();
        newDish.setId(newDishKey);
        newDish.setName(dishName);
        newDish.setActive(true);
        newDish.setCreationUserId(user.getUid());
        newDish.setCreationTimestamp(String.valueOf(new Date().getTime()));

        Map<String, Object> postValues = newDish.toMap();

        Log.d(TAG, "/" + Configs.NODE_DISHES + "/" + newDishKey);
        Log.d(TAG, "/" + Configs.NODE_USER_DISHES + "/" + user.getUid() + "/" + newDishKey);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Configs.NODE_DISHES + "/" + newDishKey, postValues);
        childUpdates.put("/" + Configs.NODE_USER_DISHES + "/" + user.getUid() + "/" + newDishKey, true);

        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("CREATED_DISH", newDish);
                    setResult(RESULT_OK, resultIntent);
                    finish();

                }
                else{

                    setResult(RESULT_CANCELED);
                    finish();

                }

            }
        });

    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NewDishActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

}
