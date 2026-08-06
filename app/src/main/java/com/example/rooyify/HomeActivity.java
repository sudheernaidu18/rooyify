package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rooyify.network.SessionManager;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SessionManager sessionManager = new SessionManager(this);
        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (sessionManager.getUserName() != null) {
            tvUserName.setText(sessionManager.getUserName());
        }

        // Streak & Compliance Dashboard
        android.widget.ProgressBar pbCompliance = findViewById(R.id.pb_compliance);
        TextView tvCompliancePct = findViewById(R.id.tv_compliance_pct);
        TextView tvStreakCount = findViewById(R.id.tv_streak_count);
        TextView tvDaysLogged = findViewById(R.id.tv_days_logged);

        kotlin.Triple<Integer, Integer, Integer> stats = sessionManager.checkAndUpdateStreak();
        int streak = stats.getFirst();
        int loggedCount = stats.getSecond();
        int compliance = stats.getThird();

        if (pbCompliance != null) pbCompliance.setProgress(compliance);
        if (tvCompliancePct != null) tvCompliancePct.setText(compliance + "%");
        if (tvStreakCount != null) tvStreakCount.setText(String.valueOf(streak));
        if (tvDaysLogged != null) tvDaysLogged.setText(loggedCount + "/7");

        // Feature Navigation
        findViewById(R.id.card_quiz).setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));
        findViewById(R.id.card_reports).setOnClickListener(v -> startActivity(new Intent(this, ReportsListActivity.class)));
        findViewById(R.id.card_book).setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
        findViewById(R.id.card_appointments).setOnClickListener(v -> startActivity(new Intent(this, MyAppointmentsActivity.class)));
        findViewById(R.id.card_upload).setOnClickListener(v -> startActivity(new Intent(this, UploadPhotoActivity.class)));
        findViewById(R.id.card_my_images).setOnClickListener(v -> startActivity(new Intent(this, HairImagesActivity.class)));
        findViewById(R.id.card_tracker).setOnClickListener(v -> startActivity(new Intent(this, ProgressTrackerActivity.class)));
        


        // Bottom Navigation
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}