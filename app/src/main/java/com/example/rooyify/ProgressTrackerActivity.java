package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

public class ProgressTrackerActivity extends AppCompatActivity {

    private String selectedPhase = "Baseline";
    private TextView tvPhaseBaseline, tvPhase3m, tvPhase6m;
    private View layoutEmptyState, layoutContentArea;
    
    // UI elements for the 4 angles
    private ImageView ivFrontal, ivLeft, ivRight, ivVertex;
    private TextView tvFrontalDate, tvLeftDate, tvRightDate, tvVertexDate;
    private TextView tvFrontalRemark, tvLeftRemark, tvRightRemark, tvVertexRemark;

    private final List<HairImage> allImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracker);

        // Back button
        FrameLayout btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Layout visibility views
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutContentArea = findViewById(R.id.layout_content_area);

        // Phase UI
        tvPhaseBaseline = findViewById(R.id.tv_phase_baseline);
        tvPhase3m = findViewById(R.id.tv_phase_3m);
        tvPhase6m = findViewById(R.id.tv_phase_6m);

        if (tvPhaseBaseline != null) tvPhaseBaseline.setOnClickListener(v -> selectPhase("Baseline"));
        if (tvPhase3m != null) tvPhase3m.setOnClickListener(v -> selectPhase("3 Months"));
        if (tvPhase6m != null) tvPhase6m.setOnClickListener(v -> selectPhase("6 Months"));

        // Image grid bindings
        ivFrontal = findViewById(R.id.iv_frontal_preview);
        ivLeft = findViewById(R.id.iv_left_preview);
        ivRight = findViewById(R.id.iv_right_preview);
        ivVertex = findViewById(R.id.iv_vertex_preview);

        // Date bindings
        tvFrontalDate = findViewById(R.id.tv_frontal_date);
        tvLeftDate = findViewById(R.id.tv_left_date);
        tvRightDate = findViewById(R.id.tv_right_date);
        tvVertexDate = findViewById(R.id.tv_vertex_date);

        // Remark bindings
        tvFrontalRemark = findViewById(R.id.tv_frontal_remark);
        tvLeftRemark = findViewById(R.id.tv_left_remark);
        tvRightRemark = findViewById(R.id.tv_right_remark);
        tvVertexRemark = findViewById(R.id.tv_vertex_remark);

        loadProgressData();
    }

    private void loadProgressData() {
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
                        allImages.clear();
                        List<HairImage> images = response.body().getImages();
                        if (images != null) {
                            allImages.addAll(images);
                        }

                        if (allImages.isEmpty()) {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            layoutContentArea.setVisibility(View.GONE);
                        } else {
                            layoutEmptyState.setVisibility(View.GONE);
                            layoutContentArea.setVisibility(View.VISIBLE);
                            bindPhaseData(selectedPhase);
                        }
                    } else {
                        Toast.makeText(ProgressTrackerActivity.this, "Failed to load progress data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProgressTrackerActivity.this, "Server error loading progress", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HairImagesResponse> call, Throwable t) {
                Toast.makeText(ProgressTrackerActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectPhase(String phase) {
        selectedPhase = phase;
        resetPhaseUI(tvPhaseBaseline);
        resetPhaseUI(tvPhase3m);
        resetPhaseUI(tvPhase6m);

        TextView selected = phase.equals("Baseline") ? tvPhaseBaseline : 
                           phase.equals("3 Months") ? tvPhase3m : tvPhase6m;
        
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_card);
            selected.setTextColor(ContextCompat.getColor(this, R.color.text_title));
            selected.setElevation(4f);
        }

        bindPhaseData(phase);
    }

    private void resetPhaseUI(TextView tv) {
        if (tv != null) {
            tv.setBackground(null);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_subtitle));
            tv.setElevation(0f);
        }
    }

    private void bindPhaseData(String phase) {
        bindAngleData(phase, "Frontal", ivFrontal, tvFrontalDate, tvFrontalRemark);
        bindAngleData(phase, "Left Lateral", ivLeft, tvLeftDate, tvLeftRemark);
        bindAngleData(phase, "Right Lateral", ivRight, tvRightDate, tvRightRemark);
        bindAngleData(phase, "Vertex", ivVertex, tvVertexDate, tvVertexRemark);
    }

    private void bindAngleData(String phase, String angle, ImageView iv, TextView tvDate, TextView tvRemark) {
        if (iv == null || tvDate == null || tvRemark == null) return;

        // Target tag in database: "Baseline_Frontal", "3 Months_Frontal", etc.
        String targetTag = phase + "_" + angle;
        HairImage matchedImage = null;

        for (HairImage img : allImages) {
            if (targetTag.equalsIgnoreCase(img.getProgressTag())) {
                matchedImage = img;
                break;
            }
        }

        if (matchedImage != null) {
            // Image exists
            tvDate.setText("Uploaded on: " + matchedImage.getUploadedAt());
            
            if (matchedImage.getImageUrl() != null) {
                Glide.with(this)
                        .load(matchedImage.getImageUrl())
                        .placeholder(R.drawable.bg_rounded_grey)
                        .into(iv);
            }

            if (matchedImage.getRemark() != null && !matchedImage.getRemark().trim().isEmpty()) {
                tvRemark.setText("Doctor: " + matchedImage.getRemark());
                tvRemark.setVisibility(View.VISIBLE);
            } else {
                tvRemark.setText("Doctor: Pending review");
                tvRemark.setVisibility(View.VISIBLE);
            }
        } else {
            // Image not uploaded yet
            iv.setImageResource(R.drawable.bg_rounded_grey);
            tvDate.setText("Not uploaded");
            tvRemark.setVisibility(View.GONE);
        }
    }
}