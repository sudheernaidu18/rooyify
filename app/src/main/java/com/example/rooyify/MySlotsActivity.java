package com.example.rooyify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.Slot;
import com.example.rooyify.network.SlotsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySlotsActivity extends AppCompatActivity {

    private RecyclerView rvMySlots;
    private SlotsAdapter adapter;
    private final List<Slot> slotList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_slots);

        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize RecyclerView
        rvMySlots = findViewById(R.id.rv_my_slots);
        rvMySlots.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SlotsAdapter(slotList);
        rvMySlots.setAdapter(adapter);

        loadMySlots();
    }

    private void loadMySlots() {
        SessionManager sm = new SessionManager(this);
        String doctorId = sm.getUserId();
        if (doctorId == null) {
            Toast.makeText(this, "Doctor session not found", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getSlots(doctorId).enqueue(new Callback<SlotsResponse>() {
            @Override
            public void onResponse(Call<SlotsResponse> call, Response<SlotsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        slotList.clear();
                        List<Slot> slots = response.body().getSlots();
                        if (slots != null) {
                            slotList.addAll(slots);
                        }
                        adapter.notifyDataSetChanged();
                        if (slotList.isEmpty()) {
                            Toast.makeText(MySlotsActivity.this, "No slots created yet", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MySlotsActivity.this, "Failed to load slots", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MySlotsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SlotsResponse> call, Throwable t) {
                Toast.makeText(MySlotsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SlotsAdapter extends RecyclerView.Adapter<SlotsAdapter.ViewHolder> {
        private final List<Slot> items;

        SlotsAdapter(List<Slot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Slot item = items.get(position);
            String formattedDateTime = item.getDate() + " at " + item.getTime();
            holder.tvTimeDate.setText(formattedDateTime);

            String status = item.getStatus();
            holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            if ("available".equalsIgnoreCase(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_done));
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_pending));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTimeDate, tvStatus;

            ViewHolder(View itemView) {
                super(itemView);
                tvTimeDate = itemView.findViewById(R.id.tv_slot_time_date);
                tvStatus = itemView.findViewById(R.id.tv_slot_status);
            }
        }
    }
}