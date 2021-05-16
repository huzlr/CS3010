
package com.fyp.variato.adapters;
        import android.content.Context;
        import android.content.Intent;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.recyclerview.widget.RecyclerView;

        import com.fyp.variato.R;
        import com.fyp.variato.activities.MainActivity;
        import com.fyp.variato.models.tableData;
        import com.fyp.variato.utils.Constants;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.database.FirebaseDatabase;

        import java.util.List;

public class tableadapter extends RecyclerView.Adapter<tableadapter.ViewHolder>{
    private List<tableData> listData;
    private final Context context;
    public tableadapter(List<tableData> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.table_data,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        tableData ld=listData.get(position);
        holder.tableno.setText(ld.getTableNo().toString());
        holder.availablility.setText(ld.getFree().toString());
        holder.tablekey=ld.getTableKey();
        if (!ld.getFree())
        {
            holder.availablility.setText("Booked");
            holder.proceed.setEnabled(false);
        }
        else {
            holder.availablility.setText("Available");
            holder.proceed.setEnabled(true);
        }

        holder.proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i =  new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                i.putExtra("tablekey", holder.tablekey.toString());
                i.putExtra("tableNo",ld.getTableNo());
                context.startActivity(i);

            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!ld.getFree()) {

                   freeTable(position,ld);
                }
                return false;
            }
        });

    }
    public void removeItem(int position) {
        listData.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
    public void freeTable(int position, tableData ld){
        FirebaseDatabase.getInstance().getReference(Constants.TABLES_)
                .child(ld.getTableKey()).child("BookingUser").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
        FirebaseDatabase.getInstance().getReference(Constants.APP_CONFIG_)
                .child(Constants.RESERVED_SEATS).setValue(Constants.reservedSeats - ld.getGuestNumbers());
        FirebaseDatabase.getInstance().getReference(Constants.TABLES_)
                .child(ld.getTableKey()).child("guestNumbers").setValue(0);
        FirebaseDatabase.getInstance().getReference(Constants.TABLES_)
                .child(ld.getTableKey()).child("free").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                listData.get(position).setFree(true);
                notifyDataSetChanged();
                Toast.makeText(context, "Booking Canceled", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tableno,availablility;
        String tablekey;
        Button view,proceed;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            tableno=(TextView)itemView.findViewById(R.id.tablenumber);
            availablility=(TextView)itemView.findViewById(R.id.availability);

            proceed=(Button) itemView.findViewById(R.id.selectedtable);
            cardView = itemView.findViewById(R.id.table_data_card);
        }
    }
}
