package com.example.rooyify;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.AddRoutineRequest;
import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.DeleteRoutineRequest;
import com.example.rooyify.network.Routine;
import com.example.rooyify.network.RoutinesResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRoutinesActivity extends AppCompatActivity {

    private RecyclerView rvRoutines;
    private View layoutEmptyState;
    private RoutinesAdapter adapter;
    private final List<Routine> routinesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_routines);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // View bindings
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        rvRoutines = findViewById(R.id.rv_routines);
        rvRoutines.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoutinesAdapter(routinesList);
        rvRoutines.setAdapter(adapter);

        // FAB listener
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_routine);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddRoutineDialog());
        }

        loadRoutines();
    }

    private void loadRoutines() {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getRoutines(userId).enqueue(new Callback<RoutinesResponse>() {
            @Override
            public void onResponse(Call<RoutinesResponse> call, Response<RoutinesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        routinesList.clear();
                        List<Routine> list = response.body().getRoutines();
                        if (list != null) {
                            routinesList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();

                        if (routinesList.isEmpty()) {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            rvRoutines.setVisibility(View.GONE);
                        } else {
                            layoutEmptyState.setVisibility(View.GONE);
                            rvRoutines.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(MyRoutinesActivity.this, "Failed to load routines", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyRoutinesActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutinesResponse> call, Throwable t) {
                Toast.makeText(MyRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRoutineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_routine, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_routine_name);
        EditText etDosage = dialogView.findViewById(R.id.et_routine_dosage);
        EditText etFrequency = dialogView.findViewById(R.id.et_routine_frequency);
        EditText etTime = dialogView.findViewById(R.id.et_routine_time);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Overriding the positive button click listener so it doesn't dismiss if input is invalid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Routine name is required");
                return;
            }

            saveRoutine(name, dosage, frequency, time, dialog);
        });
    }

    private void saveRoutine(String name, String dosage, String frequency, String time, AlertDialog dialog) {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        AddRoutineRequest request = new AddRoutineRequest(userId, name, dosage, frequency, time);
        RetrofitClient.INSTANCE.getInstance().addRoutine(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(MyRoutinesActivity.this, "Routine added successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadRoutines();
                } else {
                    Toast.makeText(MyRoutinesActivity.this, "Failed to save routine", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(MyRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRoutine(String routineId) {
        DeleteRoutineRequest request = new DeleteRoutineRequest(routineId);
        RetrofitClient.INSTANCE.getInstance().deleteRoutine(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(MyRoutinesActivity.this, "Routine deleted", Toast.LENGTH_SHORT).show();
                    loadRoutines();
                } else {
                    Toast.makeText(MyRoutinesActivity.this, "Failed to delete routine", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(MyRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.ViewHolder> {
        private final List<Routine> items;

        RoutinesAdapter(List<Routine> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Routine item = items.get(position);
            holder.tvName.setText(item.getName());
            
            // Format details
            String details = "";
            if (!item.getDosage().isEmpty()) {
                details += item.getDosage();
            }
            if (!item.getFrequency().isEmpty()) {
                if (!details.isEmpty()) {
                    details += " • ";
                }
                details += item.getFrequency();
            }
            if (details.isEmpty()) {
                details = "No dosage/frequency set";
            }
            holder.tvDetails.setText(details);

            // Time badge
            if (item.getTime().isEmpty()) {
                holder.tvTime.setVisibility(View.GONE);
            } else {
                holder.tvTime.setText(item.getTime());
                holder.tvTime.setVisibility(View.VISIBLE);
            }

            // Delete click
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(MyRoutinesActivity.this)
                        .setTitle("Delete Routine")
                        .setMessage("Are you sure you want to delete this routine?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteRoutine(item.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails, tvTime;
            ImageView btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_routine_name);
                tvDetails = itemView.findViewById(R.id.tv_routine_details);
                tvTime = itemView.findViewById(R.id.tv_routine_time);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}