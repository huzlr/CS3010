package com.fyp.variato.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fyp.variato.models.TableModel;
import com.fyp.variato.models.tableData;
import com.fyp.variato.utils.Constants;
import com.fyp.variato.utils.Progress;
import com.fyp.variato.R;
import com.fyp.variato.models.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignIn";
    MaterialButton btnSignIn, btnSignUp;
    TextInputEditText edEmail, edPass;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        listeners();
        addTablesinDB();
        //addConfigurationinDB();
        fetchTodayName();
    }


    private void listeners() {

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        catch (Exception e){}
        if(mAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        btnSignIn.setOnClickListener(view -> {
            if (edEmail.getText().toString().equals("")) {
                edEmail.setError("Required");
                return;
            }
            if (edPass.getText().toString().equals("")) {
                edPass.setError("Required");
                return;
            }
            authListener();
        });

        btnSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            //overridePendingTransition(R.anim.slide_up, R.anim.nothing);
        });
    }

    private void authListener() {
        Progress.ShowProgress(SignInActivity.this,"Please wait checking your account");
        mAuth.signInWithEmailAndPassword(edEmail.getText().toString(), edPass.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Progress.CloseProgress();
                        Log.d("MyApp", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        finish();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        Progress.CloseProgress();

                        Log.w("MyApp", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getData(FirebaseUser user) {
        mDatabase.getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "onDataChange: Key" + dataSnapshot.getKey());
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    Log.e(TAG, "onDataChange: " + userModel.getEmail());

                    if (mAuth.getCurrentUser() != null) {
                        //startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Error Occurred : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        btnSignIn = findViewById(R.id.btn_signin);
        btnSignUp = findViewById(R.id.btn_signup);
        edEmail = findViewById(R.id.txt_email);
        edPass = findViewById(R.id.txt_pass);
    }
    void addTablesinDB(){
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference(Constants.TABLES_).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("TAG", "snapshot ;" + snapshot);
                if (!snapshot.exists()) {
                    for (int i = 1; i <= 30; i++) {
                        String uid = mDatabase.getReference(Constants.TABLES_).push().getKey();
                        TableModel tableModel = new TableModel(uid, i, true,0);
                        tableData d = new tableData(true, uid, (long) i, 0);
                        mDatabase.getReference(Constants.TABLES_).child(uid).setValue(d);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    void addConfigurationinDB(){
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference(Constants.APP_CONFIG_).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("TAG", "snapshot ;" + snapshot);
                if (!snapshot.exists()) {
                    mDatabase.getReference(Constants.APP_CONFIG_).child(Constants.CAFE_TOTAL_SEATS).setValue(100);
                    mDatabase.getReference(Constants.APP_CONFIG_).child(Constants.RESERVED_SEATS).setValue(0);
                }
                else {
                    try {
                        Constants.totalSeats = snapshot.child(Constants.CAFE_TOTAL_SEATS).getValue(Integer.class);
                        Constants.reservedSeats = snapshot.child(Constants.RESERVED_SEATS).getValue(Integer.class);
                    }
                    catch (Exception e){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void fetchTodayName(){
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        Log.d("TAG","today name : "+dayOfTheWeek);
        Constants.dayOfWeekName = dayOfTheWeek;
    }
}