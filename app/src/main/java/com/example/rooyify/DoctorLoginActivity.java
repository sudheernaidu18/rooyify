package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.LoginRequest;
import com.example.rooyify.network.LoginResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        // Back Button
        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Doctor Login Button
        Button btnLogin = findViewById(R.id.btn_login);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(DoctorLoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginRequest request = new LoginRequest(email, password);
                
                RetrofitClient.INSTANCE.getInstance().loginUser(request).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                // Make sure role is actually "doctor"
                                if ("doctor".equals(response.body().getUser().getRole())) {
                                    User user = response.body().getUser();
                                    if (user != null) {
                                        SessionManager sm = new SessionManager(DoctorLoginActivity.this);
                                        sm.saveUserSession(user.getId(), user.getName(), user.getEmail(), user.getRole());
                                    }
                                    
                                    Toast.makeText(DoctorLoginActivity.this, "Doctor Login Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(DoctorLoginActivity.this, DoctorDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(DoctorLoginActivity.this, "This account is a patient account, not a doctor.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(DoctorLoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DoctorLoginActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(DoctorLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Register Link
        TextView tvRegister = findViewById(R.id.tv_register);
        if (tvRegister != null) {
            tvRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorLoginActivity.this, SignUpActivity.class);
                    intent.putExtra("role", "doctor");
                    startActivity(intent);
                }
            });
        }
    }
}