package com.fyp.variato.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fyp.variato.utils.Constants;
import com.fyp.variato.utils.Progress;
import com.fyp.variato.R;
import com.fyp.variato.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText edEmail, edPass, edPass2;
    TextInputEditText edName, edPhone;
    MaterialButton btnSignUp, btnBackSignIn;
    private static final String TAG = "SignUpActivity";
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore ;
    FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
        listeners();
    }


    private void signUpListener() {
        Progress.ShowProgress(this,"Processing your data please wait a while");
        mAuth.createUserWithEmailAndPassword(edEmail.getText().toString(), edPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            UserModel userModel = new UserModel(edName.getText().toString().trim(),
                                    currentUser.getUid(),edPhone.getText().toString().trim(),
                                    edEmail.getText().toString().trim());

                            insertUserDataInDatabase(userModel);

                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            Progress.CloseProgress();
                        }
                        else {
                            Progress.CloseProgress();
                        }

                    }
                });
    }

    private void listeners() {
        btnSignUp.setOnClickListener(view -> {

            if (edName.getText().toString().equals("")) {
                edName.setError("Required!");
                return;
            }
            if (edPhone.getText().toString().equals("")) {
                edPhone.setError("Required!");
                return;
            }
            if (edEmail.getText().toString().equals("")) {
                edEmail.setError("Required!");
                return;
            }
            if (edPass.getText().toString().equals("")) {
                edPass.setError("Required!");
                return;
            }
            if (edPass2.getText().toString().equals("")) {
                edPass2.setError("Required!");
                return;
            }

            if (edPass.getText().toString().equals(edPass2.getText().toString())) {
                signUpListener();
            } else {
                Toast.makeText(SignUpActivity.this, "Password must match!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackSignIn.setOnClickListener(view -> finish());
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        edEmail = findViewById(R.id.txt_email);
        edName = findViewById(R.id.txt_name);
        edPhone = findViewById(R.id.txt_phone);
        edPass = findViewById(R.id.txt_pass);
        edPass2 = findViewById(R.id.txt_pass_2);
        btnSignUp = findViewById(R.id.btn_signup);
        btnBackSignIn = findViewById(R.id.btn_signin_back);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.nothing, R.anim.slide_down);
    }

    void insertUserDataInDatabase(UserModel userModel){

        firebaseDatabase.getReference(Constants.USERS).child(userModel.getUserUID()).setValue(userModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG","User Data inserted....");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","User Data failed to inserted...."+e.getLocalizedMessage());

            }
        });
    }
}