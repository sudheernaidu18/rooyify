package com.example.rooyify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.Appointment;
import com.example.rooyify.network.AppointmentsResponse;
import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.UpdateAppointmentStatusRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private View layoutNoRequests;
    private RequestsAdapter adapter;
    private final List<Appointment> requestsList = new ArrayList<>();
    private String doctorId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_requests);

        SessionManager sm = new SessionManager(this);
        doctorId = sm.getUserId();

        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvRequests = findViewById(R.id.rv_appointment_requests);
        layoutNoRequests = findViewById(R.id.layout_no_requests);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(requestsList);
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor session error", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getAppointments(null, doctorId).enqueue(new Callback<AppointmentsResponse>() {
            @Override
            public void onResponse(Call<AppointmentsResponse> call, Response<AppointmentsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        requestsList.clear();
                        List<Appointment> apps = response.body().getAppointments();
                        if (apps != null) {
                            requestsList.addAll(apps);
                        }
                        adapter.notifyDataSetChanged();

                        if (requestsList.isEmpty()) {
                            layoutNoRequests.setVisibility(View.VISIBLE);
                            rvRequests.setVisibility(View.GONE);
                        } else {
                            layoutNoRequests.setVisibility(View.GONE);
                            rvRequests.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(AppointmentRequestsActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AppointmentRequestsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentsResponse> call, Throwable t) {
                Toast.makeText(AppointmentRequestsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRequestStatus(String appointmentId, String newStatus) {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(appointmentId, newStatus);
        RetrofitClient.INSTANCE.getInstance().updateAppointmentStatus(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(AppointmentRequestsActivity.this, "Appointment updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadRequests();
                } else {
                    Toast.makeText(AppointmentRequestsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(AppointmentRequestsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {
        private final List<Appointment> items;

        RequestsAdapter(List<Appointment> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment item = items.get(position);
            holder.tvPatientName.setText(item.getPatientName() != null ? item.getPatientName() : "Patient");
            String dt = item.getDate() + " at " + item.getTime();
            holder.tvRequestTime.setText(dt);

            String status = item.getStatus();
            if ("pending".equalsIgnoreCase(status)) {
                holder.layoutActions.setVisibility(View.VISIBLE);
                holder.tvStatus.setVisibility(View.GONE);
            } else {
                holder.layoutActions.setVisibility(View.GONE);
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
                if ("approved".equalsIgnoreCase(status)) {
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.status_done));
                } else {
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_rounded_grey);
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.text_subtitle));
                }
            }

            holder.btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateRequestStatus(item.getId(), "approved");
                }
            });

            holder.btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateRequestStatus(item.getId(), "rejected");
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPatientName, tvRequestTime, tvStatus;
            View layoutActions;
            View btnApprove, btnReject;

            ViewHolder(View itemView) {
                super(itemView);
                tvPatientName = itemView.findViewById(R.id.tv_patient_name);
                tvRequestTime = itemView.findViewById(R.id.tv_request_time);
                tvStatus = itemView.findViewById(R.id.tv_request_status);
                layoutActions = itemView.findViewById(R.id.layout_actions);
                btnApprove = itemView.findViewById(R.id.btn_approve);
                btnReject = itemView.findViewById(R.id.btn_reject);
            }
        }
    }
}