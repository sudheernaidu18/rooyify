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
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.os.CountDownTimer;

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

    private LinearLayout layoutRegisterForm;
    private LinearLayout layoutOtpVerification;
    
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextView tvOtpTimer;
    private TextView tvOtpResend;
    private TextView tvTestOtpBadge;
    private View layoutTestOtpBadge;
    private TextView btnOtpBackToEdit;
    private Button btnVerifyOtp;
    
    private CountDownTimer countDownTimer;
    private String currentCorrectOtp;
    private RegisterRequest pendingRegisterRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Registration form views
        layoutRegisterForm = findViewById(R.id.layout_register_form);
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

            pendingRegisterRequest = new RegisterRequest(name, email, phone, place, dob, role, password);
            executeFinalRegistration(pendingRegisterRequest);
        });

        // OTP verification views
        layoutOtpVerification = findViewById(R.id.layout_otp_verification);
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        tvOtpTimer = findViewById(R.id.tv_otp_timer);
        tvOtpResend = findViewById(R.id.tv_otp_resend);
        tvTestOtpBadge = findViewById(R.id.tv_test_otp_badge);
        layoutTestOtpBadge = findViewById(R.id.layout_test_otp_badge);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnOtpBackToEdit = findViewById(R.id.btn_otp_back_to_edit);

        setupOtpInputs();

        btnVerifyOtp.setOnClickListener(v -> verifyOtpAndRegister());
        btnOtpBackToEdit.setOnClickListener(v -> showRegisterFormView());
        tvOtpResend.setOnClickListener(v -> resendOtpCode());
    }

    private void setupOtpInputs() {
        setupOtpEditText(etOtp1, null, etOtp2);
        setupOtpEditText(etOtp2, etOtp1, etOtp3);
        setupOtpEditText(etOtp3, etOtp2, etOtp4);
        setupOtpEditText(etOtp4, etOtp3, etOtp5);
        setupOtpEditText(etOtp5, etOtp4, etOtp6);
        setupOtpEditText(etOtp6, etOtp5, null);
    }

    private void setupOtpEditText(final EditText current, final EditText prev, final EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
            }
        });

        current.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (current.getText().length() == 0 && prev != null) {
                        prev.requestFocus();
                        prev.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        current.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                current.selectAll();
            }
        });
    }

    private void sendOtpAndVerify(String phone) {
        Toast.makeText(SignUpActivity.this, "Sending OTP...", Toast.LENGTH_SHORT).show();
        
        com.example.rooyify.network.SendOtpRequest otpRequest = new com.example.rooyify.network.SendOtpRequest(phone);
        
        RetrofitClient.INSTANCE.getInstance().sendOtp(otpRequest).enqueue(new Callback<com.example.rooyify.network.OtpResponse>() {
            @Override
            public void onResponse(Call<com.example.rooyify.network.OtpResponse> call, Response<com.example.rooyify.network.OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    String generatedOtp = response.body().getOtp();
                    Boolean smsSent = response.body().getSms_sent();
                    showOtpVerificationView(generatedOtp, smsSent);
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Failed to send OTP";
                    Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.rooyify.network.OtpResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Failed to send OTP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpVerificationView(String correctOtp, Boolean smsSent) {
        currentCorrectOtp = correctOtp;
        layoutRegisterForm.setVisibility(View.GONE);
        layoutOtpVerification.setVisibility(View.VISIBLE);
        tvTestOtpBadge.setText("Testing Code: " + correctOtp);
        
        if (smsSent != null && smsSent) {
            layoutTestOtpBadge.setVisibility(View.GONE);
        } else {
            layoutTestOtpBadge.setVisibility(View.VISIBLE);
        }
        
        // Reset OTP fields
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();

        startResendTimer();
    }

    private void showRegisterFormView() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        layoutOtpVerification.setVisibility(View.GONE);
        layoutRegisterForm.setVisibility(View.VISIBLE);
    }

    private void startResendTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvOtpTimer.setVisibility(View.VISIBLE);
        tvOtpResend.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvOtpTimer.setText("Resend code in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvOtpTimer.setVisibility(View.GONE);
                tvOtpResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void resendOtpCode() {
        if (pendingRegisterRequest == null) return;
        Toast.makeText(SignUpActivity.this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        
        com.example.rooyify.network.SendOtpRequest otpRequest = new com.example.rooyify.network.SendOtpRequest(pendingRegisterRequest.getPhone());
        
        RetrofitClient.INSTANCE.getInstance().sendOtp(otpRequest).enqueue(new Callback<com.example.rooyify.network.OtpResponse>() {
            @Override
            public void onResponse(Call<com.example.rooyify.network.OtpResponse> call, Response<com.example.rooyify.network.OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    currentCorrectOtp = response.body().getOtp();
                    tvTestOtpBadge.setText("Testing Code: " + currentCorrectOtp);
                    
                    Boolean smsSent = response.body().getSms_sent();
                    if (smsSent != null && smsSent) {
                        layoutTestOtpBadge.setVisibility(View.GONE);
                    } else {
                        layoutTestOtpBadge.setVisibility(View.VISIBLE);
                    }
                    
                    Toast.makeText(SignUpActivity.this, "OTP Resent Successfully", Toast.LENGTH_SHORT).show();
                    startResendTimer();
                    
                    etOtp1.setText("");
                    etOtp2.setText("");
                    etOtp3.setText("");
                    etOtp4.setText("");
                    etOtp5.setText("");
                    etOtp6.setText("");
                    etOtp1.requestFocus();
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Failed to resend OTP";
                    Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.rooyify.network.OtpResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Failed to resend OTP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtpAndRegister() {
        String enteredOtp = etOtp1.getText().toString().trim() +
                etOtp2.getText().toString().trim() +
                etOtp3.getText().toString().trim() +
                etOtp4.getText().toString().trim() +
                etOtp5.getText().toString().trim() +
                etOtp6.getText().toString().trim();

        if (enteredOtp.length() < 6) {
            Toast.makeText(this, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(currentCorrectOtp)) {
            Toast.makeText(SignUpActivity.this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show();
            executeFinalRegistration(pendingRegisterRequest);
        } else {
            Toast.makeText(this, "Invalid OTP. Please check the code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeFinalRegistration(RegisterRequest request) {
        Toast.makeText(SignUpActivity.this, "Registering account...", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SignUpActivity.this, "Server error during registration", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}