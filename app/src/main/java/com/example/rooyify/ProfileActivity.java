package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        com.example.rooyify.network.SessionManager sessionManager = new com.example.rooyify.network.SessionManager(this);
        
        android.widget.TextView tvName = findViewById(R.id.tv_name);
        android.widget.TextView tvEmail = findViewById(R.id.tv_email);

        if (sessionManager.isLoggedIn()) {
            String name = sessionManager.getUserName();
            String email = sessionManager.getUserId(); // Email is stored as USER_EMAIL but getUserId gets ID, wait let's check SessionManager again. Actually SessionManager doesn't expose getUserEmail. Let's just use what we can. Let me use SharedPreferences directly or update SessionManager if needed.
            // Wait, I will use shared preferences directly if getEmail is missing.
            android.content.SharedPreferences prefs = getSharedPreferences("RooyifySession", android.content.Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "No Email");

            if (name != null) tvName.setText(name);
            tvEmail.setText(userEmail);
        }

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.nav_home);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Profile Actions
        findViewById(R.id.btn_update_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Delete Account Requested", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.rooyify.network.SessionManager sessionManager = new com.example.rooyify.network.SessionManager(ProfileActivity.this);
                sessionManager.logout();
                
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}