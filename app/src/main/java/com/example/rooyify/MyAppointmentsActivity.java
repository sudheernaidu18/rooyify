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

import com.example.rooyify.network.Appointment;
import com.example.rooyify.network.AppointmentsResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAppointmentsActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private AppointmentsAdapter adapter;
    private final List<Appointment> appointmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvAppointments = findViewById(R.id.rv_my_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentsAdapter(appointmentsList);
        rvAppointments.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getAppointments(userId, null).enqueue(new Callback<AppointmentsResponse>() {
            @Override
            public void onResponse(Call<AppointmentsResponse> call, Response<AppointmentsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        appointmentsList.clear();
                        List<Appointment> apps = response.body().getAppointments();
                        if (apps != null) {
                            appointmentsList.addAll(apps);
                        }
                        adapter.notifyDataSetChanged();
                        if (appointmentsList.isEmpty()) {
                            Toast.makeText(MyAppointmentsActivity.this, "No appointments booked yet", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyAppointmentsActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyAppointmentsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentsResponse> call, Throwable t) {
                Toast.makeText(MyAppointmentsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {
        private final List<Appointment> items;

        AppointmentsAdapter(List<Appointment> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment item = items.get(position);
            holder.tvDoctorName.setText(item.getDoctorName() != null ? item.getDoctorName() : "Doctor");
            String dt = item.getDate() + " at " + item.getTime();
            holder.tvTime.setText(dt);

            String status = item.getStatus();
            holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            if ("approved".equalsIgnoreCase(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_done));
            } else if ("rejected".equalsIgnoreCase(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_rounded_grey);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.text_subtitle));
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
            TextView tvDoctorName, tvTime, tvStatus;

            ViewHolder(View itemView) {
                super(itemView);
                tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
                tvTime = itemView.findViewById(R.id.tv_appointment_time);
                tvStatus = itemView.findViewById(R.id.tv_status);
            }
        }
    }
}