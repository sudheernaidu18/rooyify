package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            if ("doctor".equals(sessionManager.getUserRole())) {
                startActivity(new Intent(MainActivity.this, DoctorDashboardActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // User Login Button
        Button btnUserLogin = findViewById(R.id.btn_user_login);
        btnUserLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Doctor Login Button
        Button btnDoctorLogin = findViewById(R.id.btn_doctor_login);
        btnDoctorLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DoctorLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}