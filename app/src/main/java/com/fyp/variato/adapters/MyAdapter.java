package com.fyp.variato.adapters;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.variato.R;
import com.fyp.variato.models.ListData;
import com.fyp.variato.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private List<ListData>listData;
    FirebaseAuth firebaseAuth;
    Context context;

    public MyAdapter(List<ListData> listData, Context context) {
        this.listData = listData;
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_data,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListData ld=listData.get(position);
        holder.txtname.setText(ld.getName());
        holder.txtdate.setText(ld.getCurrentDate());
        holder.txttime.setText(ld.getCurrentTime());
        holder.tableNo.setText("Reserved Table # : "+ld.getTableNo());
        holder.personsCount.setText("Allowed Person : "+ld.getPersons());
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                freeTable(position,ld, holder.cancel);
            }
        });
        Log.d("TAG","booking time :"+ld.getBookingDataAndTime());

    }
    public void freeTable(int position, ListData listData, Button btn){
        btn.setEnabled(false);

        FirebaseDatabase.getInstance().getReference(Constants.USERS)
                .child(firebaseAuth.getCurrentUser().getUid()).child("Booking")
                .child(listData.getBookingKey())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Log.d("TAG","last  value :"+(listData.getThisSlotReservedSeats() - listData.getPersons()));

                FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                        .child(listData.getBookedTimeSlotKey()).child(listData.getCurrentTime())
                        .child("BookedSeatsInThisSlot").get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                DataSnapshot dataSnapshot = task.getResult();

                                Log.d("TAG","snap  ..............."+dataSnapshot);

                                int finalvalue = dataSnapshot.getValue(Integer.class);
                                FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                                        .child(listData.getBookedTimeSlotKey())
                                        .child(listData.getCurrentTime())
                                        .child("BookedSeatsInThisSlot").setValue(finalvalue - listData.getPersons())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("TAG","last  value :"+(finalvalue - listData.getPersons()));
                                                removeItem(position);
                                                Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show();
                                                btn.setEnabled(true);
                                            }
                                        });
                            }
                        });

            }
        });

    }
    public void removeItem(int position) {
        listData.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtname,txttime,txtdate,tableNo,personsCount;
        Intent iin;
        Bundle b;
        Button view,cancel;
        public ViewHolder(View itemView) {
            super(itemView);
            txtname=(TextView)itemView.findViewById(R.id.nameperson);
            txttime=(TextView)itemView.findViewById(R.id.time);
            txtdate=(TextView)itemView.findViewById(R.id.date);
            tableNo=(TextView)itemView.findViewById(R.id.tableNo);
            personsCount=(TextView)itemView.findViewById(R.id.personData);
            cancel=(Button) itemView.findViewById(R.id.cancel);
        }
    }
}
