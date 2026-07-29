package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.HairImage;
import com.example.rooyify.network.HairImagesResponse;
import com.example.rooyify.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientHairImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HairImageAdapter adapter;
    private final List<HairImage> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_hair_images);

        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_hair_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HairImageAdapter(imageList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientHairImages();
    }

    private void loadPatientHairImages() {
        String filterUserId = getIntent().getStringExtra("user_id");
        RetrofitClient.INSTANCE.getInstance().getHairImages(filterUserId).enqueue(new Callback<HairImagesResponse>() {
            @Override
            public void onResponse(Call<HairImagesResponse> call, Response<HairImagesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        imageList.clear();
                        List<HairImage> list = response.body().getImages();
                        if (list != null) {
                            imageList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();
                        if (imageList.isEmpty()) {
                            Toast.makeText(PatientHairImagesActivity.this, "No patient uploads found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PatientHairImagesActivity.this, "Failed to load patient images", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientHairImagesActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HairImagesResponse> call, Throwable t) {
                Toast.makeText(PatientHairImagesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class HairImageAdapter extends RecyclerView.Adapter<HairImageAdapter.ViewHolder> {
        private final List<HairImage> items;

        HairImageAdapter(List<HairImage> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hair_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HairImage item = items.get(position);
            String title = (item.getPatientName() != null ? item.getPatientName() : "Patient") + " (" + item.getProgressTag().replace("_", " - ") + ")";
            holder.tvName.setText(title);

            boolean hasRemark = item.getRemark() != null && !item.getRemark().trim().isEmpty();
            if (hasRemark) {
                holder.tvStatus.setText("Remark Added");
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivStatusIcon.setColorFilter(getResources().getColor(R.color.status_done));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_done));
            } else {
                holder.tvStatus.setText("Awaiting Review");
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock);
                holder.ivStatusIcon.setColorFilter(getResources().getColor(R.color.status_pending));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_pending));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PatientHairImagesActivity.this, ImageReviewActivity.class);
                intent.putExtra("image_id", item.getId());
                intent.putExtra("patient_name", item.getPatientName());
                intent.putExtra("progress_tag", item.getProgressTag());
                intent.putExtra("image_url", item.getImageUrl());
                intent.putExtra("remark", item.getRemark());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            ImageView ivStatusIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_patient_name);
                tvStatus = itemView.findViewById(R.id.tv_status_text);
                ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            }
        }
    }
}