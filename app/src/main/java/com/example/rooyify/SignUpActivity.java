package com.example.rooyify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.InputType;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.RegisterRequest;
import com.example.rooyify.network.RetrofitClient;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        EditText etName = findViewById(R.id.et_full_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPhone = findViewById(R.id.et_phone);
        EditText etPlace = findViewById(R.id.et_place);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etConfirmPassword = findViewById(R.id.et_confirm_password);
        ImageView ivTogglePassword = findViewById(R.id.iv_toggle_password);
        ImageView ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, ivTogglePassword));
        ivToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword));

        ivTogglePassword.setAlpha(0.5f);
        ivToggleConfirmPassword.setAlpha(0.5f);
        
        LinearLayout layoutDob = findViewById(R.id.layout_dob);
        TextView tvDobValue = findViewById(R.id.tv_dob_value);
        
        layoutDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        tvDobValue.setText(selectedDate);
                        tvDobValue.setTextColor(getResources().getColor(R.color.text_title));
                    }, year, month, day);
            datePickerDialog.show();
        });

        Button btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String place = etPlace.getText().toString().trim();
            String dob = tvDobValue.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || place.isEmpty() || 
                password.isEmpty() || dob.equals("Select Date of Birth")) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            String intentRole = getIntent().getStringExtra("role");
            String role = (intentRole != null) ? intentRole : "user";

            RegisterRequest request = new RegisterRequest(name, email, phone, place, dob, role, password);

            RetrofitClient.INSTANCE.getInstance().registerUser(request).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if ("success".equals(response.body().getStatus())) {
                            showSuccessDialog();
                        } else {
                            Toast.makeText(SignUpActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Registration successful! Please login.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon) {
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            icon.setAlpha(1.0f);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setAlpha(0.5f);
        }
        editText.setSelection(editText.getText().length());
    }
}