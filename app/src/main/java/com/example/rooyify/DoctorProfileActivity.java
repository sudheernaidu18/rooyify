package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DoctorProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        com.example.rooyify.network.SessionManager sessionManager = new com.example.rooyify.network.SessionManager(this);
        
        android.widget.TextView tvName = findViewById(R.id.tv_name);
        android.widget.TextView tvEmail = findViewById(R.id.tv_email);

        android.widget.EditText etName = findViewById(R.id.et_profile_name);
        android.widget.EditText etPhone = findViewById(R.id.et_profile_phone);
        android.widget.EditText etPlace = findViewById(R.id.et_profile_place);
        android.widget.EditText etDob = findViewById(R.id.et_profile_dob);

        if (sessionManager.isLoggedIn()) {
            String name = sessionManager.getUserName();
            String userEmail = sessionManager.getUserEmail();
            String phone = sessionManager.getUserPhone();
            String place = sessionManager.getUserPlace();
            String dob = sessionManager.getUserDob();

            if (name != null) {
                tvName.setText(name);
                etName.setText(name);
            }
            if (userEmail != null) tvEmail.setText(userEmail);
            if (phone != null) etPhone.setText(phone);
            if (place != null) etPlace.setText(place);
            if (dob != null) etDob.setText(dob);
        }

        // DOB Click Date Picker
        etDob.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    DoctorProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedMonth = String.format("%02d", selectedMonth + 1);
                        String formattedDay = String.format("%02d", selectedDay);
                        etDob.setText(selectedYear + "-" + formattedMonth + "-" + formattedDay);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Back Button
        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Bottom Navigation
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorDashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // Profile Actions
        findViewById(R.id.btn_update_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String place = etPlace.getText().toString().trim();
                String dob = etDob.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty() || place.isEmpty() || dob.isEmpty()) {
                    Toast.makeText(DoctorProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = sessionManager.getUserId();
                if (userId == null) {
                    Toast.makeText(DoctorProfileActivity.this, "Session error", Toast.LENGTH_SHORT).show();
                    return;
                }

                com.example.rooyify.network.UpdateProfileRequest request = 
                        new com.example.rooyify.network.UpdateProfileRequest(userId, name, phone, place, dob);

                Toast.makeText(DoctorProfileActivity.this, "Updating profile...", Toast.LENGTH_SHORT).show();

                com.example.rooyify.network.RetrofitClient.INSTANCE.getInstance().updateProfile(request)
                        .enqueue(new retrofit2.Callback<com.example.rooyify.network.BasicResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, retrofit2.Response<com.example.rooyify.network.BasicResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    if ("success".equals(response.body().getStatus())) {
                                        sessionManager.updateUserSession(name, phone, place, dob);
                                        tvName.setText(name);
                                        Toast.makeText(DoctorProfileActivity.this, "Profile Updated successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(DoctorProfileActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(DoctorProfileActivity.this, "Server error during update", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, Throwable t) {
                                Toast.makeText(DoctorProfileActivity.this, "Update failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        findViewById(R.id.btn_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            com.example.rooyify.network.SessionManager sm = new com.example.rooyify.network.SessionManager(this);
            sm.logout();
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showDeleteAccountDialog() {
        android.widget.EditText etPasswordInput = new android.widget.EditText(this);
        etPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPasswordInput.setHint("Enter password");

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        params.topMargin = paddingPx / 2;
        params.bottomMargin = paddingPx / 2;
        etPasswordInput.setLayoutParams(params);
        container.addView(etPasswordInput);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is permanent. Please enter your password to confirm:")
                .setView(container)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String password = etPasswordInput.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(DoctorProfileActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executeDeleteAccount(password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeDeleteAccount(String password) {
        com.example.rooyify.network.SessionManager sessionManager = new com.example.rooyify.network.SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.rooyify.network.DeleteAccountRequest request = 
                new com.example.rooyify.network.DeleteAccountRequest(userId, password);

        Toast.makeText(this, "Deleting account...", Toast.LENGTH_SHORT).show();

        com.example.rooyify.network.RetrofitClient.INSTANCE.getInstance().deleteAccount(request)
                .enqueue(new retrofit2.Callback<com.example.rooyify.network.BasicResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, retrofit2.Response<com.example.rooyify.network.BasicResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                sessionManager.logout();
                                Toast.makeText(DoctorProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(DoctorProfileActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DoctorProfileActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DoctorProfileActivity.this, "Server error during account deletion", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.rooyify.network.BasicResponse> call, Throwable t) {
                        Toast.makeText(DoctorProfileActivity.this, "Deletion failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}