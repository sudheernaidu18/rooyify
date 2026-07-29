package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rooyify.network.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for 2 seconds then navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(this);
            
            if (sessionManager.isLoggedIn()) {
                // If already logged in, go to respective dashboard
                String role = sessionManager.getUserRole();
                if ("doctor".equals(role)) {
                    startActivity(new Intent(SplashActivity.this, DoctorDashboardActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                }
            } else {
                // If not logged in, go to selection screen
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 2000);
    }
}