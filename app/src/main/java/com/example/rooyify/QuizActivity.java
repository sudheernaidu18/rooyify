package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.SubmitQuizRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionCount, tvQuestion;
    private TextView tvOption1, tvOption2, tvOption3, tvOption4;
    private ProgressBar progressBar;
    private int currentQuestion = 1;

    // Track user answers
    private final Map<String, String> answersMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize Views
        tvQuestionCount = findViewById(R.id.tv_question_count);
        tvQuestion = findViewById(R.id.tv_question);
        tvOption1 = findViewById(R.id.tv_option_1);
        tvOption2 = findViewById(R.id.tv_option_2);
        tvOption3 = findViewById(R.id.tv_option_3);
        tvOption4 = findViewById(R.id.tv_option_4);
        progressBar = findViewById(R.id.quiz_progress);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Set Click Listeners for Options
        View.OnClickListener optionClickListener = v -> {
            TextView optionText = (TextView) v;
            String answer = optionText.getText().toString();
            answersMap.put("q" + currentQuestion, answer);

            if (currentQuestion == 1) {
                showQuestion2();
            } else if (currentQuestion == 2) {
                showQuestion3();
            } else if (currentQuestion == 3) {
                showQuestion4();
            } else if (currentQuestion == 4) {
                showQuestion5();
            } else if (currentQuestion == 5) {
                showQuestion6();
            } else if (currentQuestion == 6) {
                showQuestion7();
            } else if (currentQuestion == 7) {
                showQuestion8();
            } else if (currentQuestion == 8) {
                showQuestion9();
            } else if (currentQuestion == 9) {
                showQuestion10();
            } else if (currentQuestion == 10) {
                showQuestion11();
            } else if (currentQuestion == 11) {
                showQuestion12();
            } else if (currentQuestion == 12) {
                showQuestion13();
            } else {
                // Final Question Answered -> Compute & Submit Quiz
                submitQuizAndGoToReport();
            }
        };

        tvOption1.setOnClickListener(optionClickListener);
        tvOption2.setOnClickListener(optionClickListener);
        tvOption3.setOnClickListener(optionClickListener);
        tvOption4.setOnClickListener(optionClickListener);

        showQuestion1();
    }

    private String calculateRiskLevel() {
        int riskScore = 0;
        for (String ans : answersMap.values()) {
            String lowercase = ans.toLowerCase();
            if (lowercase.contains("severe") || lowercase.contains("both parents") || 
                lowercase.contains("constantly") || lowercase.contains("daily") || 
                lowercase.contains("often") || lowercase.contains("8+")) {
                riskScore += 3;
            } else if (lowercase.contains("sometimes") || lowercase.contains("occasionally") || 
                       lowercase.contains("moderate") || lowercase.contains("one side") || 
                       lowercase.contains("medium") || lowercase.contains("6to8")) {
                riskScore += 2;
            } else {
                riskScore += 1;
            }
        }
        
        if (riskScore >= 26) {
            return "high";
        } else if (riskScore >= 16) {
            return "moderate";
        } else {
            return "low";
        }
    }

    private void submitQuizAndGoToReport() {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String riskLevel = calculateRiskLevel();
        String answersJson = new Gson().toJson(answersMap);

        SubmitQuizRequest request = new SubmitQuizRequest(userId, riskLevel, answersJson);

        RetrofitClient.INSTANCE.getInstance().submitQuiz(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Intent intent = new Intent(QuizActivity.this, ReportActivity.class);
                    intent.putExtra("risk_level", riskLevel);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(QuizActivity.this, "Failed to submit quiz report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(QuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestion1() {
        currentQuestion = 1;
        tvQuestionCount.setText(R.string.quiz_question_count_1);
        tvQuestion.setText(R.string.quiz_question_1);
        tvOption1.setText(R.string.quiz_option_oily);
        tvOption2.setText(R.string.quiz_option_dry);
        tvOption3.setText(R.string.quiz_option_normal);
        tvOption4.setText(R.string.quiz_option_itchy);
        progressBar.setProgress(8);
    }

    private void showQuestion2() {
        currentQuestion = 2;
        tvQuestionCount.setText(R.string.quiz_question_count_2);
        tvQuestion.setText(R.string.quiz_question_2);
        tvOption1.setText(R.string.quiz_option_8plus);
        tvOption2.setText(R.string.quiz_option_6to8);
        tvOption3.setText(R.string.quiz_option_4to6);
        tvOption4.setText(R.string.quiz_option_less4);
        progressBar.setProgress(15);
    }

    private void showQuestion3() {
        currentQuestion = 3;
        tvQuestionCount.setText(R.string.quiz_question_count_3);
        tvQuestion.setText(R.string.quiz_question_3);
        tvOption1.setText(R.string.quiz_option_rarely);
        tvOption2.setText(R.string.quiz_option_sometimes);
        tvOption3.setText(R.string.quiz_option_frequently);
        tvOption4.setText(R.string.quiz_option_severe);
        progressBar.setProgress(23);
    }

    private void showQuestion4() {
        currentQuestion = 4;
        tvQuestionCount.setText(R.string.quiz_question_count_4);
        tvQuestion.setText(R.string.quiz_question_4);
        tvOption1.setText(R.string.quiz_option_no);
        tvOption2.setText(R.string.quiz_option_maybe);
        tvOption3.setText(R.string.quiz_option_one_side);
        tvOption4.setText(R.string.quiz_option_both_parents);
        progressBar.setProgress(31);
    }

    private void showQuestion5() {
        currentQuestion = 5;
        tvQuestionCount.setText(R.string.quiz_question_count_5);
        tvQuestion.setText(R.string.quiz_question_5);
        tvOption1.setText(R.string.quiz_option_stress_rarely);
        tvOption2.setText(R.string.quiz_option_stress_occasionally);
        tvOption3.setText(R.string.quiz_option_stress_often);
        tvOption4.setText(R.string.quiz_option_stress_constantly);
        progressBar.setProgress(38);
    }

    private void showQuestion6() {
        currentQuestion = 6;
        tvQuestionCount.setText(R.string.quiz_question_count_6);
        tvQuestion.setText(R.string.quiz_question_6);
        tvOption1.setText(R.string.quiz_option_yes);
        tvOption2.setText(R.string.quiz_option_partially);
        tvOption3.setText(R.string.quiz_option_rarely_diet);
        tvOption4.setText(R.string.quiz_option_no_diet);
        progressBar.setProgress(46);
    }

    private void showQuestion7() {
        currentQuestion = 7;
        tvQuestionCount.setText(R.string.quiz_question_count_7);
        tvQuestion.setText(R.string.quiz_question_7);
        tvOption1.setText(R.string.quiz_option_none);
        tvOption2.setText(R.string.quiz_option_iron);
        tvOption3.setText(R.string.quiz_option_vitamin_d);
        tvOption4.setText(R.string.quiz_option_multiple);
        progressBar.setProgress(54);
    }

    private void showQuestion8() {
        currentQuestion = 8;
        tvQuestionCount.setText(R.string.quiz_question_count_8);
        tvQuestion.setText(R.string.quiz_question_8);
        tvOption1.setText(R.string.quiz_option_fine);
        tvOption2.setText(R.string.quiz_option_medium);
        tvOption3.setText(R.string.quiz_option_thick);
        tvOption4.setText(R.string.quiz_option_coarse);
        progressBar.setProgress(62);
    }

    private void showQuestion9() {
        currentQuestion = 9;
        tvQuestionCount.setText(R.string.quiz_question_count_9);
        tvQuestion.setText(R.string.quiz_question_9);
        tvOption1.setText(R.string.quiz_option_no_thinning);
        tvOption2.setText(R.string.quiz_option_mild);
        tvOption3.setText(R.string.quiz_option_moderate);
        tvOption4.setText(R.string.quiz_option_severe_thinning);
        progressBar.setProgress(69);
    }

    private void showQuestion10() {
        currentQuestion = 10;
        tvQuestionCount.setText(R.string.quiz_question_count_10);
        tvQuestion.setText(R.string.quiz_question_10);
        tvOption1.setText(R.string.quiz_option_never);
        tvOption2.setText(R.string.quiz_option_sometimes_dandruff);
        tvOption3.setText(R.string.quiz_option_often_dandruff);
        tvOption4.setText(R.string.quiz_option_always);
        progressBar.setProgress(77);
    }

    private void showQuestion11() {
        currentQuestion = 11;
        tvQuestionCount.setText(R.string.quiz_question_count_11);
        tvQuestion.setText(R.string.quiz_question_11);
        tvOption1.setText(R.string.quiz_option_styling_rarely);
        tvOption2.setText(R.string.quiz_option_styling_sometimes);
        tvOption3.setText(R.string.quiz_option_styling_often);
        tvOption4.setText(R.string.quiz_option_styling_daily);
        progressBar.setProgress(85);
    }

    private void showQuestion12() {
        currentQuestion = 12;
        tvQuestionCount.setText(R.string.quiz_question_count_12);
        tvQuestion.setText(R.string.quiz_question_12);
        tvOption1.setText(R.string.quiz_option_pain_no);
        tvOption2.setText(R.string.quiz_option_pain_slightly);
        tvOption3.setText(R.string.quiz_option_pain_moderate);
        tvOption4.setText(R.string.quiz_option_pain_severe);
        progressBar.setProgress(92);
    }

    private void showQuestion13() {
        currentQuestion = 13;
        tvQuestionCount.setText(R.string.quiz_question_count_13);
        tvQuestion.setText(R.string.quiz_question_13);
        tvOption1.setText(R.string.quiz_option_final_1);
        tvOption2.setText(R.string.quiz_option_final_2);
        tvOption3.setText(R.string.quiz_option_final_3);
        tvOption4.setText(R.string.quiz_option_final_4);
        progressBar.setProgress(100);
    }
}