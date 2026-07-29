package com.example.rooyify;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SaveHairRemarkRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageReviewActivity extends AppCompatActivity {

    private ImageView ivHairImage;
    private EditText etRemark;
    private String imageId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_review);

        ivHairImage = findViewById(R.id.iv_hair_image);
        etRemark = findViewById(R.id.et_remark);

        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Get intent extras
        imageId = getIntent().getStringExtra("image_id");
        String patientName = getIntent().getStringExtra("patient_name");
        String progressTag = getIntent().getStringExtra("progress_tag");
        String imageUrl = getIntent().getStringExtra("image_url");
        String remark = getIntent().getStringExtra("remark");

        // Set current values
        if (remark != null) {
            etRemark.setText(remark);
        }

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_images)
                    .into(ivHairImage);
        }

        findViewById(R.id.btn_save_remark).setOnClickListener(v -> saveRemarkOnServer());
    }

    private void saveRemarkOnServer() {
        String remark = etRemark.getText().toString().trim();
        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(this, "Error: Image ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        SaveHairRemarkRequest request = new SaveHairRemarkRequest(imageId, remark);
        RetrofitClient.INSTANCE.getInstance().saveHairRemark(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(ImageReviewActivity.this, "Medical Remark Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ImageReviewActivity.this, "Failed to save remark", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(ImageReviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}