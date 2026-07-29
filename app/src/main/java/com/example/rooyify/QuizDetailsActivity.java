package com.example.rooyify;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class QuizDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);

        LinearLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get report data from Intent
        String riskLevel = getIntent().getStringExtra("risk_level");
        String answersJson = getIntent().getStringExtra("answers");

        TextView tvRisk = findViewById(R.id.tv_risk_title);
        if (riskLevel != null && tvRisk != null) {
            tvRisk.setText("Risk Level: " + riskLevel.toUpperCase());
            if ("high".equalsIgnoreCase(riskLevel)) {
                tvRisk.setTextColor(Color.parseColor("#EF4444"));
            } else if ("moderate".equalsIgnoreCase(riskLevel)) {
                tvRisk.setTextColor(Color.parseColor("#F97316"));
            } else {
                tvRisk.setTextColor(Color.parseColor("#22C55E"));
            }
        }

        if (answersJson != null) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> answersMap = new Gson().fromJson(answersJson, type);
                
                if (answersMap != null) {
                    setTextValue(R.id.tv_scalp_type, answersMap.get("q1"));
                    setTextValue(R.id.tv_sleep_hours, answersMap.get("q2"));
                    setTextValue(R.id.tv_hair_fall, answersMap.get("q3"));
                    setTextValue(R.id.tv_family_history, answersMap.get("q4"));
                    setTextValue(R.id.tv_stress_level, answersMap.get("q5"));
                    setTextValue(R.id.tv_diet, answersMap.get("q6"));
                    setTextValue(R.id.tv_deficiencies, answersMap.get("q7"));
                    setTextValue(R.id.tv_hair_texture, answersMap.get("q8"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setTextValue(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) {
            tv.setText(val);
        }
    }
}