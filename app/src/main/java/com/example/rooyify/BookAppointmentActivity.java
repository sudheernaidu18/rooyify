package com.example.rooyify;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.BookAppointmentRequest;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.Slot;
import com.example.rooyify.network.SlotsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAppointmentActivity extends AppCompatActivity {

    private RecyclerView rvAvailableSlots;
    private View layoutNoSlots;
    private SlotsAdapter adapter;
    private final List<Slot> slotsList = new ArrayList<>();
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        SessionManager sm = new SessionManager(this);
        currentUserId = sm.getUserId();

        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvAvailableSlots = findViewById(R.id.rv_available_slots);
        layoutNoSlots = findViewById(R.id.layout_no_slots);

        rvAvailableSlots.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SlotsAdapter(slotsList);
        rvAvailableSlots.setAdapter(adapter);

        loadAvailableSlots();
    }

    private void loadAvailableSlots() {
        RetrofitClient.INSTANCE.getInstance().getSlots(null).enqueue(new Callback<SlotsResponse>() {
            @Override
            public void onResponse(Call<SlotsResponse> call, Response<SlotsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        slotsList.clear();
                        List<Slot> slots = response.body().getSlots();
                        if (slots != null) {
                            slotsList.addAll(slots);
                        }
                        
                        adapter.notifyDataSetChanged();
                        
                        if (slotsList.isEmpty()) {
                            layoutNoSlots.setVisibility(View.VISIBLE);
                            rvAvailableSlots.setVisibility(View.GONE);
                        } else {
                            layoutNoSlots.setVisibility(View.GONE);
                            rvAvailableSlots.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(BookAppointmentActivity.this, "Failed to load slots", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookAppointmentActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SlotsResponse> call, Throwable t) {
                Toast.makeText(BookAppointmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookAppointment(Slot slot) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        BookAppointmentRequest request = new BookAppointmentRequest(currentUserId, slot.getDoctorId(), slot.getId());
        
        RetrofitClient.INSTANCE.getInstance().bookAppointment(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(BookAppointmentActivity.this, "Failed: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookAppointmentActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(BookAppointmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success_appointment);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView btnOk = dialog.findViewById(R.id.btn_dialog_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    private class SlotsAdapter extends RecyclerView.Adapter<SlotsAdapter.ViewHolder> {
        private final List<Slot> items;

        SlotsAdapter(List<Slot> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Slot item = items.get(position);
            holder.tvDoctorName.setText(item.getDoctorName() != null ? item.getDoctorName() : "Doctor");
            String dt = item.getDate() + " at " + item.getTime();
            holder.tvSlotTime.setText(dt);
            
            holder.btnBookNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookAppointment(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDoctorName, tvSlotTime;
            Button btnBookNow;

            ViewHolder(View itemView) {
                super(itemView);
                tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
                tvSlotTime = itemView.findViewById(R.id.tv_slot_time);
                btnBookNow = itemView.findViewById(R.id.btn_book_now);
            }
        }
    }
}