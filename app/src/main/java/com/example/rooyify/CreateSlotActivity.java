package com.example.rooyify;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.CreateSlotRequest;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateSlotActivity extends AppCompatActivity {

    private TextView tvSelectedTime;
    private int selectedHour = 15;
    private int selectedMinute = 44;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_slot);

        tvSelectedTime = findViewById(R.id.tv_selected_time);
        CalendarView calendarView = findViewById(R.id.calendar_view);

        // Initialize selected date to today
        Calendar calendar = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", 
                calendar.get(Calendar.YEAR), 
                calendar.get(Calendar.MONTH) + 1, 
                calendar.get(Calendar.DAY_OF_MONTH));

        // Listen for calendar date changes
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            }
        });

        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Time Picker
        tvSelectedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Create Slot Button
        findViewById(R.id.btn_create_slot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSlotOnServer();
            }
        });
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeDisplay();
                    }
                }, selectedHour, selectedMinute, false);
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int displayHour = selectedHour > 12 ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
        tvSelectedTime.setText(String.format(Locale.getDefault(), "%d:%02d %s", displayHour, selectedMinute, amPm));
    }

    private void createSlotOnServer() {
        SessionManager sm = new SessionManager(this);
        String doctorId = sm.getUserId();
        if (doctorId == null) {
            Toast.makeText(this, "Error: Doctor session not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = tvSelectedTime.getText().toString();
        CreateSlotRequest request = new CreateSlotRequest(doctorId, selectedDate, time);

        RetrofitClient.INSTANCE.getInstance().createSlot(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(CreateSlotActivity.this, "Failed: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateSlotActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(CreateSlotActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("✅ Slot Created Successfully")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }
}