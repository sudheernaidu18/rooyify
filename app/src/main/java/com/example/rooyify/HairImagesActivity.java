package com.example.rooyify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rooyify.network.HairImage;
import com.example.rooyify.network.HairImagesResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HairImagesActivity extends AppCompatActivity {

    private RecyclerView rvHairImages;
    private View layoutNoAlbums;
    private HairImagesAdapter adapter;
    private final List<HairImage> hairImagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hair_images);

        FrameLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvHairImages = findViewById(R.id.rv_hair_images_list);
        layoutNoAlbums = findViewById(R.id.layout_no_albums);

        rvHairImages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HairImagesAdapter(hairImagesList);
        rvHairImages.setAdapter(adapter);

        loadHairImages();
    }

    private void loadHairImages() {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getHairImages(userId).enqueue(new Callback<HairImagesResponse>() {
            @Override
            public void onResponse(Call<HairImagesResponse> call, Response<HairImagesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        hairImagesList.clear();
                        List<HairImage> list = response.body().getImages();
                        if (list != null) {
                            hairImagesList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();

                        if (hairImagesList.isEmpty()) {
                            layoutNoAlbums.setVisibility(View.VISIBLE);
                            rvHairImages.setVisibility(View.GONE);
                        } else {
                            layoutNoAlbums.setVisibility(View.GONE);
                            rvHairImages.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(HairImagesActivity.this, "Failed to load hair images", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HairImagesActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HairImagesResponse> call, Throwable t) {
                Toast.makeText(HairImagesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class HairImagesAdapter extends RecyclerView.Adapter<HairImagesAdapter.ViewHolder> {
        private final List<HairImage> items;

        HairImagesAdapter(List<HairImage> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_hair_album, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HairImage item = items.get(position);
            holder.tvTitle.setText(item.getProgressTag().replace("_", " - "));
            holder.tvDate.setText("Uploaded at: " + item.getUploadedAt());
            
            if (item.getRemark() != null && !item.getRemark().trim().isEmpty()) {
                holder.tvRemark.setText("Doctor remark: " + item.getRemark());
                holder.tvRemark.setTextColor(getResources().getColor(R.color.status_done));
            } else {
                holder.tvRemark.setText("Doctor remark: Pending review");
                holder.tvRemark.setTextColor(getResources().getColor(R.color.status_pending));
            }

            // Load remote image URL using Glide
            if (item.getImageUrl() != null) {
                // Pointing to local development URL, Glide handles it automatically
                Glide.with(HairImagesActivity.this)
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.bg_rounded_grey)
                        .into(holder.ivThumb);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvTitle, tvDate, tvRemark;

            ViewHolder(View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_album_thumb);
                tvTitle = itemView.findViewById(R.id.tv_album_title);
                tvDate = itemView.findViewById(R.id.tv_album_date);
                tvRemark = itemView.findViewById(R.id.tv_album_remark);
            }
        }
    }
}