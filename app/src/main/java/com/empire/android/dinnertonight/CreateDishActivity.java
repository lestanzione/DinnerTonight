package com.empire.android.dinnertonight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateDishActivity extends AppCompatActivity {

    private static final String TAG = CreateDishActivity.class.getSimpleName();

    private EditText createDishNameEditText;
    private Button createDishCreateButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dish);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            showLoginActivity();
        }

        createDishNameEditText = (EditText) findViewById(R.id.createDishNameEditText);
        createDishCreateButton = (Button) findViewById(R.id.createDishCreateButton);

        createDishCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDish();
            }
        });

    }

    private void showLoginActivity(){

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void createDish(){

        String dishName = createDishNameEditText.getText().toString().trim();

        if(dishName.trim().isEmpty()){
            Toast.makeText(CreateDishActivity.this, "Give a name to your new dish!", Toast.LENGTH_SHORT).show();
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

        Log.d(TAG, "/dishes/" + newDishKey);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/dishes/" + newDishKey, postValues);

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

}
