package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.SessionManager;

public class DoctorDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Fetch and display logged in doctor's name
        SessionManager sm = new SessionManager(this);
        TextView tvDoctorName = findViewById(R.id.tv_doctor_name);
        if (tvDoctorName != null && sm.getUserName() != null) {
            tvDoctorName.setText(sm.getUserName());
        }

        // Card Click Listeners
        View cardPatientPortfolios = findViewById(R.id.card_patient_portfolios);
        if (cardPatientPortfolios != null) {
            cardPatientPortfolios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorDashboardActivity.this, PatientPortfoliosActivity.class);
                    startActivity(intent);
                }
            });
        }

        View cardCreateSlot = findViewById(R.id.card_create_slot);
        if (cardCreateSlot != null) {
            cardCreateSlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorDashboardActivity.this, CreateSlotActivity.class);
                    startActivity(intent);
                }
            });
        }

        View cardMySlots = findViewById(R.id.card_my_slots);
        if (cardMySlots != null) {
            cardMySlots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorDashboardActivity.this, MySlotsActivity.class);
                    startActivity(intent);
                }
            });
        }

        View cardRequests = findViewById(R.id.card_requests);
        if (cardRequests != null) {
            cardRequests.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorDashboardActivity.this, AppointmentRequestsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Bottom Navigation
        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DoctorDashboardActivity.this, DoctorProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}