package com.fyp.variato.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fyp.variato.models.ListData;
import com.fyp.variato.adapters.MyAdapter;
import com.fyp.variato.R;
import com.fyp.variato.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class bookings extends AppCompatActivity {
    private List<ListData> listData;
    private RecyclerView rv;
    private MyAdapter adapter;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference booking_reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        rv=(RecyclerView)findViewById(R.id.recyclerview);
        rv.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        user= mAuth.getCurrentUser();
        rv.setLayoutManager(new LinearLayoutManager(this));
        listData=new ArrayList<>();


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
            finish();
            Intent intent = new Intent(getApplicationContext(), bookings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final DatabaseReference nm= FirebaseDatabase.getInstance().getReference(Constants.USERS)
                .child(user.getUid())
                .child("Booking");
        nm.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    listData.clear();
                    Log.d("TAG","data :"+dataSnapshot.toString());
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()){

                        ListData l=npsnapshot.getValue(ListData.class);
                        listData.add(l);
                        Log.d("TAG","data 1 :"+npsnapshot.toString());
                    }
                    adapter=new MyAdapter(listData,bookings.this);
                    rv.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}