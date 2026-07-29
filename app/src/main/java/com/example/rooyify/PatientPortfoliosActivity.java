package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.PatientsResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientPortfoliosActivity extends AppCompatActivity {

    private RecyclerView rvPatients;
    private View layoutEmptyState;
    private PatientsAdapter adapter;
    private final List<User> patientsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_portfolios);

        // Toolbar back
        FrameLayout btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        layoutEmptyState = findViewById(R.id.layout_empty_state);
        rvPatients = findViewById(R.id.rv_patients);
        rvPatients.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatientsAdapter(patientsList);
        rvPatients.setAdapter(adapter);

        loadPatients();
    }

    private void loadPatients() {
        RetrofitClient.INSTANCE.getInstance().getPatients().enqueue(new Callback<PatientsResponse>() {
            @Override
            public void onResponse(Call<PatientsResponse> call, Response<PatientsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        patientsList.clear();
                        List<User> list = response.body().getPatients();
                        if (list != null) {
                            patientsList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();

                        if (patientsList.isEmpty()) {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            rvPatients.setVisibility(View.GONE);
                        } else {
                            layoutEmptyState.setVisibility(View.GONE);
                            rvPatients.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(PatientPortfoliosActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientPortfoliosActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PatientsResponse> call, Throwable t) {
                Toast.makeText(PatientPortfoliosActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.ViewHolder> {
        private final List<User> items;

        PatientsAdapter(List<User> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_portfolio, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User item = items.get(position);
            holder.tvName.setText(item.getName());
            
            // Set details: "place • phone"
            String details = item.getPlace();
            if (item.getPhone() != null && !item.getPhone().isEmpty()) {
                if (details != null && !details.isEmpty()) {
                    details += " • " + item.getPhone();
                } else {
                    details = item.getPhone();
                }
            }
            holder.tvDetails.setText(details);

            // Initials circle logic
            String initial = "P";
            if (item.getName() != null && !item.getName().trim().isEmpty()) {
                initial = item.getName().trim().substring(0, 1).toUpperCase();
            }
            holder.tvInitials.setText(initial);

            // Helper to prepare intent for PatientPortfolioDetailActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PatientPortfoliosActivity.this, PatientPortfolioDetailActivity.class);
                intent.putExtra("user_id", item.getId());
                intent.putExtra("patient_name", item.getName());
                intent.putExtra("patient_email", item.getEmail());
                intent.putExtra("patient_phone", item.getPhone());
                intent.putExtra("patient_place", item.getPlace());
                intent.putExtra("patient_dob", item.getDob());
                intent.putExtra("selected_tab", "profile");
                startActivity(intent);
            });

            // View Quiz Reports
            holder.btnViewReports.setOnClickListener(v -> {
                Intent intent = new Intent(PatientPortfoliosActivity.this, PatientPortfolioDetailActivity.class);
                intent.putExtra("user_id", item.getId());
                intent.putExtra("patient_name", item.getName());
                intent.putExtra("patient_email", item.getEmail());
                intent.putExtra("patient_phone", item.getPhone());
                intent.putExtra("patient_place", item.getPlace());
                intent.putExtra("patient_dob", item.getDob());
                intent.putExtra("selected_tab", "quiz");
                startActivity(intent);
            });

            // View Hair Images
            holder.btnViewImages.setOnClickListener(v -> {
                Intent intent = new Intent(PatientPortfoliosActivity.this, PatientPortfolioDetailActivity.class);
                intent.putExtra("user_id", item.getId());
                intent.putExtra("patient_name", item.getName());
                intent.putExtra("patient_email", item.getEmail());
                intent.putExtra("patient_phone", item.getPhone());
                intent.putExtra("patient_place", item.getPlace());
                intent.putExtra("patient_dob", item.getDob());
                intent.putExtra("selected_tab", "photos");
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInitials, tvName, tvDetails;
            LinearLayout btnViewReports, btnViewImages;

            ViewHolder(View itemView) {
                super(itemView);
                tvInitials = itemView.findViewById(R.id.tv_initials);
                tvName = itemView.findViewById(R.id.tv_patient_name);
                tvDetails = itemView.findViewById(R.id.tv_patient_details);
                btnViewReports = itemView.findViewById(R.id.btn_view_reports);
                btnViewImages = itemView.findViewById(R.id.btn_view_images);
            }
        }
    }
}
