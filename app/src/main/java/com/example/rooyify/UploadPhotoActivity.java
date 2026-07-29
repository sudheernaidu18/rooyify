package com.example.rooyify;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UploadPhotoActivity extends AppCompatActivity {

    private String selectedPhase = "Baseline";
    private TextView tvPhaseBaseline, tvPhase3m, tvPhase6m;
    private TextView tvCaptureDate;
    
    private final Map<String, Uri> imageUris = new HashMap<>();
    private String currentSelectingAngle = "";

    // Angle slot views
    private ImageView ivFrontal, ivLeft, ivRight, ivVertex;
    private View llFrontalPlace, llLeftPlace, llRightPlace, llVertexPlace;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && !currentSelectingAngle.isEmpty()) {
                        imageUris.put(currentSelectingAngle, uri);
                        updateImageUI(currentSelectingAngle, uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Phase UI
        tvPhaseBaseline = findViewById(R.id.tv_phase_baseline);
        tvPhase3m = findViewById(R.id.tv_phase_3m);
        tvPhase6m = findViewById(R.id.tv_phase_6m);

        tvPhaseBaseline.setOnClickListener(v -> selectPhase("Baseline"));
        tvPhase3m.setOnClickListener(v -> selectPhase("3 Months"));
        tvPhase6m.setOnClickListener(v -> selectPhase("6 Months"));

        // Date Picker
        tvCaptureDate = findViewById(R.id.tv_capture_date);
        tvCaptureDate.setOnClickListener(v -> showDatePicker());

        // Initialize grid slots
        initAngleSlot(R.id.slot_frontal, "Frontal");
        initAngleSlot(R.id.slot_left, "Left Lateral");
        initAngleSlot(R.id.slot_right, "Right Lateral");
        initAngleSlot(R.id.slot_vertex, "Vertex");

        findViewById(R.id.btn_upload_album).setOnClickListener(v -> {
            if (imageUris.size() < 4) {
                Toast.makeText(this, "Please select all 4 angles to continue", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Uploading clinical album for " + selectedPhase + "...", Toast.LENGTH_SHORT).show();
            uploadAlbum();
        });
    }

    private void uploadAlbum() {
        com.example.rooyify.network.SessionManager sm = new com.example.rooyify.network.SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadNextAngle(userId, 0);
    }

    private void uploadNextAngle(String userId, int index) {
        String[] angles = {"Frontal", "Left Lateral", "Right Lateral", "Vertex"};
        if (index >= angles.length) {
            Toast.makeText(this, "✅ Clinical album uploaded successfully!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String angle = angles[index];
        Uri uri = imageUris.get(angle);
        if (uri == null) {
            Toast.makeText(this, "Error finding image for " + angle, Toast.LENGTH_SHORT).show();
            return;
        }

        java.io.File file = getFileFromUri(uri, angle + "_img.jpg");
        if (file == null) {
            Toast.makeText(this, "Error processing image for " + angle, Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        String progressTag = selectedPhase + "_" + angle;
        okhttp3.RequestBody userIdBody = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, userId);
        okhttp3.RequestBody tagBody = okhttp3.RequestBody.create(okhttp3.MultipartBody.FORM, progressTag);

        com.example.rooyify.network.RetrofitClient.INSTANCE.getInstance().uploadHairImage(userIdBody, tagBody, body).enqueue(new retrofit2.Callback<com.example.rooyify.network.BasicResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, retrofit2.Response<com.example.rooyify.network.BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    uploadNextAngle(userId, index + 1);
                } else {
                    Toast.makeText(UploadPhotoActivity.this, "Failed to upload " + angle, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, Throwable t) {
                Toast.makeText(UploadPhotoActivity.this, "Network error uploading " + angle + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private java.io.File getFileFromUri(Uri uri, String tempFileName) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            java.io.File file = new java.io.File(getCacheDir(), tempFileName);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initAngleSlot(int slotId, String angleName) {
        View slot = findViewById(slotId);
        TextView tvName = slot.findViewById(R.id.tv_angle_name);
        tvName.setText(angleName);
        
        ImageView ivPreview = slot.findViewById(R.id.iv_angle_preview);
        View llPlaceholder = slot.findViewById(R.id.ll_angle_placeholder);
        
        if (angleName.equals("Frontal")) {
            ivFrontal = ivPreview;
            llFrontalPlace = llPlaceholder;
        } else if (angleName.equals("Left Lateral")) {
            ivLeft = ivPreview;
            llLeftPlace = llPlaceholder;
        } else if (angleName.equals("Right Lateral")) {
            ivRight = ivPreview;
            llRightPlace = llPlaceholder;
        } else {
            ivVertex = ivPreview;
            llVertexPlace = llPlaceholder;
        }

        slot.findViewById(R.id.layout_click_area).setOnClickListener(v -> openPicker(angleName));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%d %s %d", 
                    dayOfMonth, getMonthName(month), year);
            tvCaptureDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }

    private void selectPhase(String phase) {
        selectedPhase = phase;
        resetPhaseUI(tvPhaseBaseline);
        resetPhaseUI(tvPhase3m);
        resetPhaseUI(tvPhase6m);

        TextView selected = phase.equals("Baseline") ? tvPhaseBaseline : 
                           phase.equals("3 Months") ? tvPhase3m : tvPhase6m;
        
        selected.setBackgroundResource(R.drawable.bg_card);
        selected.setTextColor(ContextCompat.getColor(this, R.color.text_title));
        selected.setElevation(4f);
    }

    private void resetPhaseUI(TextView tv) {
        tv.setBackground(null);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_subtitle));
        tv.setElevation(0f);
    }

    private void openPicker(String angle) {
        currentSelectingAngle = angle;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerLauncher.launch(intent);
    }

    private void updateImageUI(String angle, Uri uri) {
        ImageView iv;
        View placeholder;
        
        if (angle.equals("Frontal")) {
            iv = ivFrontal;
            placeholder = llFrontalPlace;
        } else if (angle.equals("Left Lateral")) {
            iv = ivLeft;
            placeholder = llLeftPlace;
        } else if (angle.equals("Right Lateral")) {
            iv = ivRight;
            placeholder = llRightPlace;
        } else {
            iv = ivVertex;
            placeholder = llVertexPlace;
        }

        if (iv != null && placeholder != null) {
            iv.setImageURI(uri);
            iv.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.GONE);
        }
    }
}