package com.fyp.variato.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "TAG";
    EditText bookName;
    Button btnBook;
    Button viewbook;
    NumberPicker numberPicker;
    FirebaseAuth mAuth;
    String j;
    DatePickerDialog picker;
    EditText date, time;
    DatabaseReference booking_reference;
    private int tableNo;
    private int selectedHours;
    private String currentDayName;
    String DATE_FORMAT_16 = "EEE, d MMM yyyy HH:mm:ss";
    int timeSlotSeatPositionCount;
    Dialog dialog;
    TextView tempTV, minTempTV, maxTempTV, weatherDescTV, cityNameTV;
    ImageView refreshBtn;

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        refreshBtn  = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = new RotateAnimation(0.0f, 360.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                animation.setRepeatCount(-1);
                animation.setDuration(1000);
                refreshBtn.startAnimation(animation);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if (checkLocationPermission()) {
                            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            readWeatherFromAPI(location);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                            }
                        }
                    }
                },2000);
            }
        });
        tempTV = (TextView) findViewById(R.id.temp_tv);
        minTempTV = (TextView) findViewById(R.id.minTempTV);
        maxTempTV = (TextView) findViewById(R.id.maxTempTv);
        weatherDescTV = (TextView) findViewById(R.id.weather_desc_tv);
        cityNameTV = (TextView) findViewById(R.id.cityNameTv);

        Intent iin = getIntent();
        Bundle b = iin.getExtras();

        ((findViewById(R.id.viewMap))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    startActivity(new Intent(getApplicationContext(), MapActivity.class));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                    }
                }
            }
        });
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
                                date.setText(dayOfTheWeek + ", " + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year + "");
                                currentDayName = dayOfTheWeek;
                                Log.e("TAG", "onDateSet: " + date);
                                Log.d("TAG", "today name : " + dayOfTheWeek);
                            }
                        }, year, month, day);

                picker.show();
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date.getText().toString().equals("")) {
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
        if (b != null) {
            j = (String) b.get("tablekey");
            tableNo = (int) b.getLong("tableNo");
            Log.e("TAG", "tablekey: " + j);
            Log.e("TAG", "tableNo: " + tableNo);
            Log.d("TAG", "Total Seats : " + Constants.totalSeats);
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        Log.e("TAG", "user: " + user.getUid().toString());

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {

            Log.d("TAG", "value : " + picker.getValue());

        });

        bookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "Total Seats : " + Constants.totalSeats);

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

                Log.d("TAG", "time edit text changed...");

                if (date.getText().toString().equals("")) {
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

            if (bookName.getText().toString().equals("")) {
                Toast.makeText(this, "Enter name to book", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.getText().toString().equals("")) {
                Toast.makeText(this, "Select data for booking", Toast.LENGTH_SHORT).show();
                return;
            }
            if (time.getText().toString().equals("")) {
                Toast.makeText(this, "Select booking time", Toast.LENGTH_SHORT).show();
                return;
            }
            /*if(!validateWorkingHours()) {
             return;
            }*/
            // 100 > 100
            if (timeSlotSeatPositionCount >= 100) {
                Toast.makeText(this, "Sorry all cafe seats are reserved right now for this timing", Toast.LENGTH_SHORT).show();
                return;
            }
            int seatLeft = 100 - timeSlotSeatPositionCount;
            Log.d("TAG", "seat left : " + seatLeft);
            if (numberPicker.getValue() > seatLeft) {
                Toast.makeText(this, "Sorry " + seatLeft + " seats are left this hours", Toast.LENGTH_SHORT).show();
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
                    booking_reference = reference.child(key);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String currentDate = new SimpleDateFormat("EEE, dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                            String currentDateAndTime = new SimpleDateFormat("MM/dd/yyyy HH:mm aa", Locale.getDefault()).format(new Date());
                            String bookingExpiryDate = new SimpleDateFormat("MM/dd/yyyy HH:mm aa", Locale.getDefault()).format(new Date(System.currentTimeMillis() + 3600 * 1000));

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

    boolean validateWorkingHours() {
        if (currentDayName.equalsIgnoreCase(Constants.Sunday)) {
            if (selectedHours < 9 || selectedHours > 20) {
                Toast.makeText(this, "Selected day timing from 9 am to 8 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (currentDayName.equalsIgnoreCase(Constants.friday) || currentDayName.equalsIgnoreCase(Constants.saturday)) {
            if (selectedHours < 9 || selectedHours > 20) {
                Toast.makeText(this, "Selected day timing from 9 am to 10 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (Constants.dayOfWeekName.equalsIgnoreCase(Constants.otherDays)) {
            if (selectedHours < 9 || selectedHours > 22) {
                Toast.makeText(this, "Selected day timing from 9 pm to 8 pm only", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    void addTimeSeatsInDB() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_);
        reference.child(date.getText().toString())
                .child(time.getText().toString())
                .child("BookedSeatsInThisSlot").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_);
                    reference.child(date.getText().toString())
                            .child(time.getText().toString())
                            .child("BookedSeatsInThisSlot").setValue(0);
                } else {

                    timeSlotSeatPositionCount = snapshot.getValue(Integer.class);
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
        } else if (item.getItemId() == R.id.menu_map) {
            if (checkLocationPermission()) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                }
            }
        } else if (item.getItemId() == R.id.menu_bookings) {
            Intent intent = new Intent(getApplicationContext(), bookings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        try {
            if (SplashscreenActivity.class != null) {
                SplashscreenActivity.checkTimeForExpiry();
            }
        } catch (Exception e) {
        }
    }

    void openTimeDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.time_picker_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Booking Time");
        builder.setView(view);

        Button cancel = view.findViewById(R.id.stay);
        Button okBtn = view.findViewById(R.id.ok_time);
        NumberPicker timeP = view.findViewById(R.id.timePicker);

        Log.d("TAG", "day name ;" + currentDayName);
        if (currentDayName.equalsIgnoreCase(Constants.saturday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(22);
            Log.d("TAG", "saturday entered.....");

        } else if (currentDayName.equalsIgnoreCase(Constants.friday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(22);
            Log.d("TAG", "friday entered.....");
        } else if (currentDayName.equalsIgnoreCase(Constants.Sunday)) {
            timeP.setMinValue(9);
            timeP.setMaxValue(20);
            Log.d("TAG", "sunday entered.....");
        } else {
            timeP.setMinValue(9);
            timeP.setMaxValue(20);
            Log.d("TAG", "other day entered.....");
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
                time.setText(timeP.getValue() + " : 00");

            }
        });
        timeP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d("TAG", "value :" + picker.getValue() + ": 00");
            }
        });
        dialog = builder.create();
        dialog.show();

    }

    boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return true;
    }

    void readWeatherFromAPI(Location location) {
        if (location == null) {
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.openweathermap.org/data/2.5/weather?";
        url += "lat=";
        url += location.getLatitude();
        url += "&lon=";
        url += location.getLongitude();
        url += "&appid=";
        url += Constants.WEATHER_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String result) {
                        Log.d("TAG", "response :: " + result);

                        JSONObject response = null;
                        try {
                            response = new JSONObject(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {

                            JSONArray weatherArray = response.getJSONArray("weather");
                            JSONObject weatherObj = weatherArray.getJSONObject(0);
                            JSONObject mainArray = response.getJSONObject("main");

                            tempTV.setText(String.valueOf(kelvinToCelsius((Double) mainArray.get("temp")))+"°");
                            minTempTV.setText(String.valueOf(kelvinToCelsius((Double) mainArray.get("temp_min")))+"° / ");
                            maxTempTV.setText(String.valueOf(kelvinToCelsius((Double) mainArray.get("temp_max")))+"°");
                            cityNameTV.setText(String.valueOf(response.get("name")));
                            weatherDescTV.setText(String.valueOf(weatherObj.get("description")));


                            Log.d(TAG, "Object 2 "+String.valueOf(mainArray.get("temp"))+" : "  +mainArray.get("feels_like")
                                    +" : "  +mainArray.get("temp_min") +" : "  +mainArray.get("temp_max"));

                            Log.d(TAG,String.valueOf(weatherObj.get("description")));
                            refreshBtn.clearAnimation();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Object 2"+e);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "error :: " + error.getLocalizedMessage());
                refreshBtn.clearAnimation();

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        switch (requestCode) {
            case 1001:

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                        // Here you write your code once permission granted

                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
                if (checkLocationPermission()) {
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    readWeatherFromAPI(location);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                    }
                }

                break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Animation animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(1000);
        refreshBtn.startAnimation(animation);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (checkLocationPermission()) {
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    readWeatherFromAPI(location);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                    }
                }
            }
        },2000);

    }
    int kelvinToCelsius(Double kelvin)
    {
        return (int) Math.round(( kelvin - 273.15F));
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 13) {
                Log.d("TAG","changing sensor value .....");

                mSensorManager.unregisterListener(mSensorListener);
                startActivity(new Intent(MainActivity.this,bookings.class));
                //Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_NORMAL);*/
                    }
                },2000);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();

    }
}