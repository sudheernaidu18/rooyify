package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DoctorProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

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
        findViewById(R.id.btn_update_profile).setOnClickListener(v -> 
            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_delete_account).setOnClickListener(v -> 
            Toast.makeText(this, "Account deletion requested", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            com.example.rooyify.network.SessionManager sessionManager = new com.example.rooyify.network.SessionManager(this);
            sessionManager.logout();
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}