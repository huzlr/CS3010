package com.fyp.variato.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.fyp.variato.R;
import com.fyp.variato.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    EditText bookName;
    Button btnBook;
    Button viewbook;
    NumberPicker numberPicker;
    FirebaseAuth mAuth;
    String j;
    DatePickerDialog picker;
    EditText date,time;
   DatabaseReference booking_reference;
    private int tableNo;
    private int selectedHours;
    private String currentDayName;
    String DATE_FORMAT_16 = "EEE, d MMM yyyy HH:mm:ss";
    int timeSlotSeatPositionCount ;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Book Table");

        numberPicker = (NumberPicker) findViewById(R.id.guest_count_picker);

        bookName = findViewById(R.id.txt_book_name);
        date = findViewById(R.id.date_12);
        time = findViewById(R.id.time_12);
        date.setInputType(InputType.TYPE_NULL);
        time.setInputType(InputType.TYPE_NULL);
        btnBook = findViewById(R.id.btn_book_now);
        viewbook = findViewById(R.id.viewbookings);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog

                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                                Date d = new Date(year, monthOfYear, dayOfMonth - 1);
                                String dayOfTheWeek = sdf.format(d);
                                date.setText(dayOfTheWeek+", "+dayOfMonth + "-" + (monthOfYear + 1) + "-" +year+"");
                                currentDayName = dayOfTheWeek;
                                Log.e("TAG", "onDateSet: "+date );
                                Log.d("TAG","today name : "+dayOfTheWeek);
                            }
                        }, year, month, day);

                picker.show();
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date.getText().toString().equals(""))
                {
                    Toast.makeText(MainActivity.this, "Select first booking date", Toast.LENGTH_SHORT).show();
                    return;
                }
                openTimeDialog();
                /*Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        time.setText( selectedHour + ":" + lseectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();*/
            }
        });
        if(b!=null) {
            j =(String) b.get("tablekey");
            tableNo = (int) b.getLong("tableNo");
            Log.e("TAG", "tablekey: "+j);
            Log.e("TAG", "tableNo: "+tableNo);
            Log.d("TAG", "Total Seats : "+ Constants.totalSeats);
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user= mAuth.getCurrentUser();
        assert user != null;
        Log.e("TAG", "user: "+user.getUid().toString() );

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {

            Log.d("TAG","value : "+ picker.getValue());

        });

        bookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "Total Seats : "+ Constants.totalSeats);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        viewbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), bookings.class));
            }
        });

        date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                /*if(date.getText().toString().equals(""))
                {
                    return;
                }
                addTimeSeatsInDB();*/
            }
        });

        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.d("TAG","time edit text changed...");

                if(date.getText().toString().equals(""))
                {
                    return;
                }
                addTimeSeatsInDB();
            }
        });
        btnBook.setOnClickListener((v -> {


            //String currentDataAndTime = new SimpleDateFormat(DATE_FORMAT_16,Locale.getDefault()).format(new Date());

            //Log.d("TAG","format : "+currentDataAndTime);

           /* if(true)
            {
                return;
            }*/

            if(bookName.getText().toString().equals(""))
            {
                Toast.makeText(this, "Enter name to book", Toast.LENGTH_SHORT).show();
                return;
            }
            if(date.getText().toString().equals(""))
            {
                Toast.makeText(this, "Select data for booking", Toast.LENGTH_SHORT).show();
                return;
            }
            if(time.getText().toString().equals("")) {
                Toast.makeText(this, "Select booking time", Toast.LENGTH_SHORT).show();
                return;
            }
            /*if(!validateWorkingHours()) {
             return;
            }*/
            // 100 > 100
            if(timeSlotSeatPositionCount >= 100) {
                Toast.makeText(this, "Sorry all cafe seats are reserved right now for this timing", Toast.LENGTH_SHORT).show();
                return;
            }
            int seatLeft = 100 - timeSlotSeatPositionCount;
            Log.d("TAG","seat left : "+seatLeft);
            if(numberPicker.getValue() > seatLeft) {
                Toast.makeText(this, "Sorry "+seatLeft+" seats are left this hours", Toast.LENGTH_SHORT).show();
                return;
            }
            ProgressDialog progressDialog;
            /**progress dialouge to put user on hold untill data is fetched from apis and websites*/
            progressDialog = new ProgressDialog(this);
            progressDialog.setOwnerActivity(MainActivity.this);
            Activity activity = progressDialog.getOwnerActivity();
            /**showing progress bar*/

            progressDialog.show();
            /**setting xml layout*/

            progressDialog.setContentView(R.layout.loading);

            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            /**setting cancalable false while loading screens*/

            progressDialog.setCancelable(false);
            progressDialog.setOnCancelListener(null);
            /**handler to put a delay so data can load and displayed*/



            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**calling functions in background to avoid ui freezing and ANR*/

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.USERS)
                            .child(mAuth.getCurrentUser().getUid()).child("Booking");
                    String key = reference.push().getKey();
                    booking_reference= reference.child(key);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String currentDate = new SimpleDateFormat("EEE, dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                            String currentDateAndTime = new SimpleDateFormat("MM/dd/yyyy HH:mm aa",Locale.getDefault()).format(new Date());
                            String bookingExpiryDate = new SimpleDateFormat("MM/dd/yyyy HH:mm aa",Locale.getDefault()).format(new Date(System.currentTimeMillis() + 3600 * 1000));

                            booking_reference.child("bookingKey").setValue(key);
                            booking_reference.child("BookedTimeSlotKey").setValue(date.getText().toString());
                            booking_reference.child("name").setValue(bookName.getText().toString());
                            booking_reference.child("persons").setValue(numberPicker.getValue());
                            booking_reference.child("tableNo").setValue(tableNo);
                            booking_reference.child("currentDate").setValue(date.getText().toString());
                            booking_reference.child("currentTime").setValue(time.getText().toString());
                            booking_reference.child("bookingDataAndTime").setValue(currentDateAndTime);
                            booking_reference.child("bookingExpiryDate").setValue(bookingExpiryDate);
                            booking_reference.child("userUID").setValue(mAuth.getCurrentUser().getUid());

                            //booking_reference.child("thisSlotReservedSeats").setValue(timeSlotSeatPositionCount + numberPicker.getValue());

                           /* int totalPersonNow = Constants.reservedSeats + numberPicker.getValue();
                            FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                                    .child(Constants.RESERVED_SEATS).setValue(totalPersonNow);*/

                            FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                            .child(date.getText().toString())
                                    .child(time.getText().toString())
                                    .child("BookedSeatsInThisSlot").setValue(timeSlotSeatPositionCount + numberPicker.getValue());


                            progressDialog.dismiss();
                            Toast.makeText(activity, "Booked successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), bookings.class));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // Your Code

                }
            }, 3500);

            /*DatabaseReference table_reference = FirebaseDatabase.getInstance().getReference("Tables").child(j);
            table_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    table_reference.child("free").setValue(false);
                    table_reference.child("guestNumbers").setValue(numberPicker.getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/

        }));

    }

    boolean validateWorkingHours(){
        if(currentDayName.equalsIgnoreCase(Constants.Sunday)) {
            if(selectedHours < 9 || selectedHours > 20) {
                Toast.makeText(this, "Selected day timing from 9 am to 8 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else if(currentDayName.equalsIgnoreCase(Constants.friday) || currentDayName.equalsIgnoreCase(Constants.saturday)) {
            if(selectedHours < 9 || selectedHours > 20) {
                Toast.makeText(this, "Selected day timing from 9 am to 10 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else if(Constants.dayOfWeekName.equalsIgnoreCase(Constants.otherDays)) {
            if(selectedHours < 9 || selectedHours > 22) {
                Toast.makeText(this, "Selected day timing from 9 pm to 8 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    void addTimeSeatsInDB(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_);
        reference.child(date.getText().toString())
                .child(time.getText().toString())
                .child("BookedSeatsInThisSlot").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_);
                    reference.child(date.getText().toString())
                            .child(time.getText().toString())
                            .child("BookedSeatsInThisSlot").setValue(0);
                }
                else {

                    timeSlotSeatPositionCount  = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_signout) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        else if(item.getItemId() == R.id.menu_bookings) {
            Intent intent = new Intent(getApplicationContext(), bookings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(SplashscreenActivity.class != null)
            {
                SplashscreenActivity.checkTimeForExpiry();
            }
        }catch (Exception e){}
    }

    void openTimeDialog(){

        View view = LayoutInflater.from(this).inflate(R.layout.time_picker_dialog, null, false);

        AlertDialog.Builder builder  = new AlertDialog.Builder(this);

        builder.setTitle("Booking Time");
        builder.setView(view);

        Button cancel = view.findViewById(R.id.stay);
        Button okBtn = view.findViewById(R.id.ok_time);
        NumberPicker timeP = view.findViewById(R.id.timePicker);

        Log.d("TAG","day name ;"+currentDayName);
        if(currentDayName.equalsIgnoreCase(Constants.saturday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(22);
            Log.d("TAG","saturday entered.....");

        }
        else if(currentDayName.equalsIgnoreCase(Constants.friday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(22);
            Log.d("TAG","friday entered.....");
        }
        else if(currentDayName.equalsIgnoreCase(Constants.Sunday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(20);
            Log.d("TAG","sunday entered.....");
        }
        else {
            timeP.setMinValue(9);
            timeP.setMaxValue(20);
            Log.d("TAG","other day entered.....");
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                selectedHours = timeP.getValue();
                time.setText( timeP.getValue() + " : 00");

            }
        });
        timeP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d("TAG","value :"+picker.getValue()+": 00");
            }
        });
        dialog = builder.create();
        dialog.show();

    }
}