package com.fyp.variato.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.fyp.variato.models.ListData;
import com.fyp.variato.R;
import com.fyp.variato.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SplashscreenActivity extends AppCompatActivity {

    Handler handler = new Handler();
    static FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        firebaseAuth = FirebaseAuth.getInstance();
        getWindow().setFlags(1024, 1024);

        getSupportActionBar().hide();


        ((findViewById(R.id.icon))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        new  Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your Code
                Intent mySuperIntent = new Intent(SplashscreenActivity.this, SignInActivity.class);
                startActivity(mySuperIntent);
                finish();
            }
        }, 2500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            checkTimeForExpiry();
        }
        catch (Exception e){}
    }

    public static void checkTimeForExpiry(){
        try {

            FirebaseDatabase.getInstance().getReference(Constants.USERS)
                    .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    DataSnapshot dataSnapshot = null;
                    try {
                        dataSnapshot = task.getResult();
                    }
                    catch (Exception e){
                        Log.e("TAG","error found: "+e.getMessage());
                        return;
                    }
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        //Log.d("TAG","snap :"+dataSnapshot1.getKey());
                        FirebaseDatabase.getInstance().getReference(Constants.USERS)
                                .child(dataSnapshot1.getKey())
                                .child("Booking").get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {

                                        for (DataSnapshot dataSnapshot2 : task.getResult().getChildren()) {
                                            FirebaseDatabase.getInstance().getReference(Constants.USERS)
                                                    .child(dataSnapshot1.getKey())
                                                    .child("Booking")
                                                    .child(dataSnapshot2.getKey()).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DataSnapshot> task) {

                                                            DataSnapshot dataSnapshot3 = task.getResult();

                                                            //Log.d("TAg","datasnapShot 3: "+dataSnapshot3.toString());

                                                            ListData listData = dataSnapshot3.getValue(ListData.class);
                                                            Log.d("TAG", "Booking time :" + listData.getBookingDataAndTime());
                                                            Log.d("TAG", "Expiry time :" + listData.getBookingExpiryDate());
                                                            if (listData.getBookingExpiryDate() != null) {
                                                                if (checkExpiryTimeOfBooking(listData.getBookingExpiryDate())) {
                                                                    Log.d("TAG", "expired >>>>>");
                                                                    freeTable(listData);
                                                                } else {
                                                                    Log.d("TAG", "not expired >>>>>");
                                                                }
                                                            }
                                                        }
                                                    });

                                        }
                                    }
                                });
                    }
                }
            });
        }
        catch (Exception e){}
    }

    public static boolean checkExpiryTimeOfBooking(String expiryDate){
        String date = "03/26/2012 11:00:00";
        String dateafter = "03/26/2012 11:59:00";
        @SuppressLint("SimpleDateFormat")
        String currentDateAndTime = new SimpleDateFormat("MM/dd/yyyy HH:mm aa", Locale.getDefault()).format(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm aa");

        Date convertedDate = new Date();
        Date convertedDate2 = new Date();
        try {
            convertedDate = dateFormat.parse(expiryDate);
            convertedDate2 = dateFormat.parse(currentDateAndTime);
            if (convertedDate2.after(convertedDate)) {
                //txtView.setText("true");
                return true;
            } else {
                return false;
                //txtView.setText("false");
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static  void freeTable( ListData listData) {
        Log.d("TAG", "last  value :" + (listData.getThisSlotReservedSeats() - listData.getPersons()));

        FirebaseDatabase.getInstance().getReference(Constants.USERS)
                .child(listData.getUserUID()).child("Booking")
                .child(listData.getBookingKey())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                                .child(listData.getBookedTimeSlotKey()).child(listData.getCurrentTime())
                                .child("BookedSeatsInThisSlot").get()
                                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot dataSnapshot) {

                                        int finalvalue = dataSnapshot.getValue(Integer.class);
                                        FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                                                .child(listData.getBookedTimeSlotKey())
                                                .child(listData.getCurrentTime())
                                                .child("BookedSeatsInThisSlot").setValue(finalvalue - listData.getPersons())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "last  value :" + (finalvalue - listData.getPersons()));
                                                    }
                                                });
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","error :"+e.getMessage());
            }
        });
    }
}