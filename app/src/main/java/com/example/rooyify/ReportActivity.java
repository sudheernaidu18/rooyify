package com.example.rooyify;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        TextView tvRiskValue = findViewById(R.id.tv_risk_value);
        
        // Get risk level from intent (default to Moderate Risk for this step)
        String risk = getIntent().getStringExtra("risk_level");
        if (risk == null) risk = "moderate"; 

        if (risk.equals("high")) {
            tvRiskValue.setText(R.string.risk_high);
            tvRiskValue.setTextColor(Color.parseColor("#EF4444")); // Red
            tvRiskValue.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_risk_high));
        } else if (risk.equals("moderate")) {
            tvRiskValue.setText(R.string.risk_moderate);
            tvRiskValue.setTextColor(Color.parseColor("#F97316")); // Orange
            tvRiskValue.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_risk_moderate));
        } else {
            tvRiskValue.setText(R.string.risk_low);
            tvRiskValue.setTextColor(Color.parseColor("#22C55E")); // Green
            tvRiskValue.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_risk_low));
        }

        Button btnBackToDashboard = findViewById(R.id.btn_back_dashboard);
        btnBackToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}