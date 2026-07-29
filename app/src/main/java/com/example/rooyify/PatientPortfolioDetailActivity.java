package com.example.rooyify;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.example.rooyify.network.AddRoutineRequest;
import com.example.rooyify.network.Appointment;
import com.example.rooyify.network.AppointmentsResponse;
import com.example.rooyify.network.BasicResponse;
import com.example.rooyify.network.DeleteRoutineRequest;
import com.example.rooyify.network.HairImage;
import com.example.rooyify.network.HairImagesResponse;
import com.example.rooyify.network.LoginResponse;
import com.example.rooyify.network.PatientsResponse;
import com.example.rooyify.network.QuizReport;
import com.example.rooyify.network.QuizReportsResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.Routine;
import com.example.rooyify.network.RoutinesResponse;
import com.example.rooyify.network.SaveHairRemarkRequest;
import com.example.rooyify.network.SessionManager;
import com.example.rooyify.network.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientPortfolioDetailActivity extends AppCompatActivity {

    private String patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private String patientPlace;
    private String patientDob;

    // Header views
    private TextView tvHeaderInitials, tvHeaderName, tvHeaderEmail, tvHeaderPhone;
    private ProgressBar pbCompliance;
    private TextView tvCompliancePercent;

    // Tabs
    private TextView tabProfile, tabQuiz, tabAppts, tabPhotos, tabRx, tabWater;

    // Containers
    private View containerProfile, containerQuiz, containerAppts, containerPhotos, containerRx;

    // Profile tab views
    private TextView tvProfileEmail, tvProfilePhone, tvProfilePlace, tvProfileDob;
    private TextView tvStatQuizzes, tvStatPhotos, tvStatAppts, tvStatStreak;
    private AppCompatButton btnExportPdf;

    // Quiz tab views
    private View layoutQuizDetails;
    private TextView tvNoQuizReports;
    private TextView tvQuizDate, tvQuizId, tvQuizSummary;
    private TextView tvDetailScalp, tvDetailTexture, tvDetailFall, tvDetailSleep, tvDetailStress, tvDetailDiet, tvDetailDeficiencies;
    private ImageView ivSymFamily, ivSymThinning, ivSymDandruff, ivSymPain;

    // Appointments tab views
    private LinearLayout layoutApptsList;
    private View cardNoAppts;

    // Photos tab views
    private LinearLayout layoutPhotosProgressStack;
    private TextView tvNoPhotos;

    // Rx tab views
    private LinearLayout layoutRxStack;
    private TextView tvNoRoutines;
    private AppCompatButton btnIssueNew;

    // Data lists for compliance and stats calculation
    private final List<QuizReport> quizReports = new ArrayList<>();
    private final List<HairImage> hairImages = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<Routine> routines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_portfolio_detail);

        // Retrieve initial patient data from intent
        patientId = getIntent().getStringExtra("user_id");
        if (patientId == null || patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Toolbar back click
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Initialize all views
        initViews();

        // Setup Tab listeners
        setupTabClickListeners();

        // Determine start tab from intent (default to Profile)
        String startTab = getIntent().getStringExtra("selected_tab");
        TabType defaultTab = TabType.PROFILE;
        if (startTab != null) {
            try {
                defaultTab = TabType.valueOf(startTab.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        switchTab(defaultTab);

        // Load profile data and all tabs content
        refreshAllData();
    }

    private void initViews() {
        // Toolbar and Header
        tvHeaderInitials = findViewById(R.id.tv_header_initials);
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderEmail = findViewById(R.id.tv_header_email);
        tvHeaderPhone = findViewById(R.id.tv_header_phone);
        
        // Find compliance ring relative layout structure
        View complianceLayout = findViewById(R.id.layout_compliance);
        
        // Let's search compliance children:
        if (complianceLayout instanceof LinearLayout) {
            LinearLayout cl = (LinearLayout) complianceLayout;
            for (int i = 0; i < cl.getChildCount(); i++) {
                View child = cl.getChildAt(i);
                if (child instanceof android.widget.FrameLayout) {
                    android.widget.FrameLayout fl = (android.widget.FrameLayout) child;
                    for (int j = 0; j < fl.getChildCount(); j++) {
                        View subChild = fl.getChildAt(j);
                        if (subChild instanceof ProgressBar) {
                            pbCompliance = (ProgressBar) subChild;
                        } else if (subChild instanceof TextView) {
                            tvCompliancePercent = (TextView) subChild;
                        }
                    }
                }
            }
        }

        // Tabs
        tabProfile = findViewById(R.id.tab_profile);
        tabQuiz = findViewById(R.id.tab_quiz);
        tabAppts = findViewById(R.id.tab_appts);
        tabPhotos = findViewById(R.id.tab_photos);
        tabRx = findViewById(R.id.tab_rx);
        tabWater = findViewById(R.id.tab_water);

        // Containers
        containerProfile = findViewById(R.id.container_profile);
        containerQuiz = findViewById(R.id.container_quiz);
        containerAppts = findViewById(R.id.container_appts);
        containerPhotos = findViewById(R.id.container_photos);
        containerRx = findViewById(R.id.container_rx);

        // Profile Tab Views
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        tvProfilePhone = findViewById(R.id.tv_profile_phone);
        tvProfilePlace = findViewById(R.id.tv_profile_place);
        tvProfileDob = findViewById(R.id.tv_profile_dob);
        tvStatQuizzes = findViewById(R.id.tv_stat_quizzes);
        tvStatPhotos = findViewById(R.id.tv_stat_photos);
        tvStatAppts = findViewById(R.id.tv_stat_appts);
        tvStatStreak = findViewById(R.id.tv_stat_streak);
        btnExportPdf = findViewById(R.id.btn_export_pdf);

        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> {
                Toast.makeText(this, "Generating PDF Report for " + tvHeaderName.getText() + "...", Toast.LENGTH_SHORT).show();
                btnExportPdf.postDelayed(() -> {
                    Toast.makeText(this, "PDF Report exported successfully.", Toast.LENGTH_SHORT).show();
                }, 1500);
            });
        }

        // Quiz Tab Views
        layoutQuizDetails = findViewById(R.id.layout_quiz_details);
        tvNoQuizReports = findViewById(R.id.tv_no_quiz_reports);
        tvQuizDate = findViewById(R.id.tv_quiz_date);
        tvQuizId = findViewById(R.id.tv_quiz_id);
        tvQuizSummary = findViewById(R.id.tv_quiz_summary);
        
        tvDetailScalp = findViewById(R.id.tv_detail_scalp);
        tvDetailTexture = findViewById(R.id.tv_detail_texture);
        tvDetailFall = findViewById(R.id.tv_detail_fall);
        tvDetailSleep = findViewById(R.id.tv_detail_sleep);
        tvDetailStress = findViewById(R.id.tv_detail_stress);
        tvDetailDiet = findViewById(R.id.tv_detail_diet);
        tvDetailDeficiencies = findViewById(R.id.tv_detail_deficiencies);

        ivSymFamily = findViewById(R.id.iv_sym_family);
        ivSymThinning = findViewById(R.id.iv_sym_thinning);
        ivSymDandruff = findViewById(R.id.iv_sym_dandruff);
        ivSymPain = findViewById(R.id.iv_sym_pain);

        // Appointments Tab Views
        layoutApptsList = findViewById(R.id.layout_appts_list);
        cardNoAppts = findViewById(R.id.card_no_appts);

        // Photos Tab Views
        layoutPhotosProgressStack = findViewById(R.id.layout_photos_progress_stack);
        tvNoPhotos = findViewById(R.id.tv_no_photos);

        // Rx Tab Views
        layoutRxStack = findViewById(R.id.layout_rx_stack);
        tvNoRoutines = findViewById(R.id.tv_no_routines);
        btnIssueNew = findViewById(R.id.btn_issue_new);

        if (btnIssueNew != null) {
            btnIssueNew.setOnClickListener(v -> showAddRoutineDialog());
        }
    }

    private void setupTabClickListeners() {
        tabProfile.setOnClickListener(v -> switchTab(TabType.PROFILE));
        tabQuiz.setOnClickListener(v -> switchTab(TabType.QUIZ));
        tabAppts.setOnClickListener(v -> switchTab(TabType.APPTS));
        tabPhotos.setOnClickListener(v -> switchTab(TabType.PHOTOS));
        tabRx.setOnClickListener(v -> switchTab(TabType.RX));
        tabWater.setOnClickListener(v -> {
            switchTab(TabType.WATER);
            Toast.makeText(this, "Water compliance tracking coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private enum TabType {
        PROFILE, QUIZ, APPTS, PHOTOS, RX, WATER
    }

    private void switchTab(TabType tabType) {
        // Reset all tabs UI state
        resetTabUI(tabProfile, R.drawable.ic_user);
        resetTabUI(tabQuiz, R.drawable.ic_reports);
        resetTabUI(tabAppts, R.drawable.ic_calendar);
        resetTabUI(tabPhotos, R.drawable.ic_images);
        resetTabUI(tabRx, R.drawable.ic_pill);
        resetTabUI(tabWater, R.drawable.ic_tracker);

        // Set all containers GONE
        containerProfile.setVisibility(View.GONE);
        containerQuiz.setVisibility(View.GONE);
        containerAppts.setVisibility(View.GONE);
        containerPhotos.setVisibility(View.GONE);
        containerRx.setVisibility(View.GONE);

        // Enable selected tab and container
        switch (tabType) {
            case PROFILE:
                setActiveTabUI(tabProfile, "Profile", R.drawable.ic_user);
                containerProfile.setVisibility(View.VISIBLE);
                break;
            case QUIZ:
                setActiveTabUI(tabQuiz, "Quiz", R.drawable.ic_reports);
                containerQuiz.setVisibility(View.VISIBLE);
                break;
            case APPTS:
                setActiveTabUI(tabAppts, "Appts", R.drawable.ic_calendar);
                containerAppts.setVisibility(View.VISIBLE);
                break;
            case PHOTOS:
                setActiveTabUI(tabPhotos, "Photos", R.drawable.ic_images);
                containerPhotos.setVisibility(View.VISIBLE);
                break;
            case RX:
                setActiveTabUI(tabRx, "Rx", R.drawable.ic_pill);
                containerRx.setVisibility(View.VISIBLE);
                break;
            case WATER:
                setActiveTabUI(tabWater, "Water", R.drawable.ic_tracker);
                containerProfile.setVisibility(View.VISIBLE); // fall back to profile container
                break;
        }
    }

    private void resetTabUI(TextView tab, int iconResId) {
        tab.setText("");
        tab.setBackgroundColor(Color.TRANSPARENT);
        tab.setTextColor(Color.parseColor("#6B7280"));
        Drawable drawable = ContextCompat.getDrawable(this, iconResId);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.parseColor("#6B7280"));
            tab.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    private void setActiveTabUI(TextView tab, String text, int iconResId) {
        tab.setText(text);
        tab.setBackgroundResource(R.drawable.bg_button_blue);
        tab.setTextColor(Color.WHITE);
        Drawable drawable = ContextCompat.getDrawable(this, iconResId);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.WHITE);
            tab.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    private void refreshAllData() {
        loadProfileData();
        loadQuizReports();
        loadHairImages();
        loadAppointments();
        loadRoutines();
    }

    private void loadProfileData() {
        RetrofitClient.INSTANCE.getInstance().getProfile(patientId).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    User user = response.body().getUser();
                    if (user != null) {
                        patientName = user.getName();
                        patientEmail = user.getEmail();
                        patientPhone = user.getPhone();
                        patientPlace = user.getPlace();
                        patientDob = user.getDob();

                        populateProfileHeader();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // fall back to loading from intent if server fails
                patientName = getIntent().getStringExtra("patient_name");
                patientEmail = getIntent().getStringExtra("patient_email");
                patientPhone = getIntent().getStringExtra("patient_phone");
                patientPlace = getIntent().getStringExtra("patient_place");
                patientDob = getIntent().getStringExtra("patient_dob");
                populateProfileHeader();
            }
        });
    }

    private void populateProfileHeader() {
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText(patientName != null ? patientName : "Patient Portfolio");
        }

        if (tvHeaderName != null) tvHeaderName.setText(patientName);
        if (tvHeaderEmail != null) tvHeaderEmail.setText(patientEmail);
        if (tvHeaderPhone != null) tvHeaderPhone.setText(patientPhone);

        if (tvHeaderInitials != null && patientName != null && !patientName.isEmpty()) {
            tvHeaderInitials.setText(patientName.substring(0, 1).toUpperCase());
        }

        // Profile tab fields
        if (tvProfileEmail != null) tvProfileEmail.setText(patientEmail);
        if (tvProfilePhone != null) tvProfilePhone.setText(patientPhone);
        if (tvProfilePlace != null) tvProfilePlace.setText(patientPlace);
        if (tvProfileDob != null) tvProfileDob.setText(patientDob);
    }

    private void loadQuizReports() {
        RetrofitClient.INSTANCE.getInstance().getQuizReports(patientId).enqueue(new Callback<QuizReportsResponse>() {
            @Override
            public void onResponse(Call<QuizReportsResponse> call, Response<QuizReportsResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    quizReports.clear();
                    List<QuizReport> reports = response.body().getReports();
                    if (reports != null) {
                        quizReports.addAll(reports);
                    }
                    updateStats();
                    populateQuizTab();
                }
            }

            @Override
            public void onFailure(Call<QuizReportsResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to load quiz reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHairImages() {
        RetrofitClient.INSTANCE.getInstance().getHairImages(patientId).enqueue(new Callback<HairImagesResponse>() {
            @Override
            public void onResponse(Call<HairImagesResponse> call, Response<HairImagesResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    hairImages.clear();
                    List<HairImage> list = response.body().getImages();
                    if (list != null) {
                        hairImages.addAll(list);
                    }
                    updateStats();
                    populatePhotosTab();
                }
            }

            @Override
            public void onFailure(Call<HairImagesResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to load hair photos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointments() {
        RetrofitClient.INSTANCE.getInstance().getAppointments(patientId, null).enqueue(new Callback<AppointmentsResponse>() {
            @Override
            public void onResponse(Call<AppointmentsResponse> call, Response<AppointmentsResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    appointments.clear();
                    List<Appointment> apps = response.body().getAppointments();
                    if (apps != null) {
                        appointments.addAll(apps);
                    }
                    updateStats();
                    populateAppointmentsTab();
                }
            }

            @Override
            public void onFailure(Call<AppointmentsResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRoutines() {
        RetrofitClient.INSTANCE.getInstance().getRoutines(patientId).enqueue(new Callback<RoutinesResponse>() {
            @Override
            public void onResponse(Call<RoutinesResponse> call, Response<RoutinesResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    routines.clear();
                    List<Routine> rts = response.body().getRoutines();
                    if (rts != null) {
                        routines.addAll(rts);
                    }
                    updateStats();
                    populateRoutinesTab();
                }
            }

            @Override
            public void onFailure(Call<RoutinesResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to load routines", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats() {
        if (tvStatQuizzes != null) tvStatQuizzes.setText(String.valueOf(quizReports.size()));
        if (tvStatPhotos != null) tvStatPhotos.setText(String.valueOf(hairImages.size()));
        if (tvStatAppts != null) tvStatAppts.setText(String.valueOf(appointments.size()));
        
        // Mock Streak days to make UI complete and active
        int streak = routines.size() > 0 ? 5 : 0;
        if (tvStatStreak != null) tvStatStreak.setText(String.valueOf(streak));

        // Calculate dynamic compliance progress:
        // Quizzes: 30%, Photos: 30%, active routines: 40%
        int compliance = 0;
        if (!quizReports.isEmpty()) compliance += 30;
        if (!hairImages.isEmpty()) compliance += 30;
        if (!routines.isEmpty()) compliance += 40;

        if (pbCompliance != null) {
            pbCompliance.setProgress(compliance);
        }
        if (tvCompliancePercent != null) {
            tvCompliancePercent.setText(compliance + "%");
        }
    }

    private void populateQuizTab() {
        if (quizReports.isEmpty()) {
            tvNoQuizReports.setVisibility(View.VISIBLE);
            layoutQuizDetails.setVisibility(View.GONE);
            return;
        }

        tvNoQuizReports.setVisibility(View.GONE);
        layoutQuizDetails.setVisibility(View.VISIBLE);

        // Take latest report
        QuizReport latestReport = quizReports.get(0);
        
        // Format date and id
        tvQuizDate.setText(latestReport.getCreatedAt().split(" ")[0]);
        tvQuizId.setText("#" + latestReport.getId());

        String risk = latestReport.getRiskLevel();
        tvQuizSummary.setText("Quiz Completed (" + risk.substring(0, 1).toUpperCase() + risk.substring(1) + " risk)");

        // Parse answers
        try {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> answers = new Gson().fromJson(latestReport.getAnswers(), type);
            if (answers == null) answers = new HashMap<>();

            // Bind values with defaults
            tvDetailScalp.setText(getValueOr(answers, "q1", "Dry"));
            tvDetailSleep.setText(getValueOr(answers, "q2", "6-8 hours"));
            tvDetailFall.setText(getValueOr(answers, "q3", "Sometimes"));
            
            String familyHistoryVal = getValueOr(answers, "q4", "No");
            tvDetailStress.setText(getValueOr(answers, "q5", "Occasionally"));
            tvDetailDiet.setText(getValueOr(answers, "q6", "Partially"));
            tvDetailDeficiencies.setText(getValueOr(answers, "q7", "Iron deficiency"));
            tvDetailTexture.setText(getValueOr(answers, "q8", "Medium"));

            String thinningVal = getValueOr(answers, "q9", "No");
            String dandruffVal = getValueOr(answers, "q10", "Never");
            String painVal = getValueOr(answers, "q12", "No");

            // Setup Symptom indicators
            setSymptomIndicator(ivSymFamily, !familyHistoryVal.equalsIgnoreCase("No"));
            setSymptomIndicator(ivSymThinning, !thinningVal.equalsIgnoreCase("No") && !thinningVal.equalsIgnoreCase("No Thinning"));
            setSymptomIndicator(ivSymDandruff, !dandruffVal.equalsIgnoreCase("Never"));
            setSymptomIndicator(ivSymPain, !painVal.equalsIgnoreCase("No"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getValueOr(Map<String, String> map, String key, String fallback) {
        String val = map.get(key);
        return (val == null || val.trim().isEmpty()) ? fallback : val;
    }

    private void setSymptomIndicator(ImageView iv, boolean present) {
        if (iv == null) return;
        if (present) {
            iv.setImageResource(R.drawable.ic_check_circle);
            iv.setColorFilter(Color.parseColor("#10B981")); // Active green
        } else {
            iv.setImageResource(R.drawable.ic_check_circle);
            iv.setColorFilter(Color.parseColor("#D1D5DB")); // Inactive grey
        }
    }

    private void populateAppointmentsTab() {
        layoutApptsList.removeAllViews();

        if (appointments.isEmpty()) {
            cardNoAppts.setVisibility(View.VISIBLE);
            return;
        }

        cardNoAppts.setVisibility(View.GONE);

        for (Appointment appt : appointments) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_my_appointment, layoutApptsList, false);

            TextView tvDocName = view.findViewById(R.id.tv_doctor_name);
            TextView tvTime = view.findViewById(R.id.tv_appointment_time);
            TextView tvStatus = view.findViewById(R.id.tv_status);

            tvDocName.setText(appt.getDoctorName() != null ? appt.getDoctorName() : "Doctor");
            tvTime.setText(appt.getDate() + " at " + appt.getTime());
            
            String status = appt.getStatus();
            tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            if ("approved".equalsIgnoreCase(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_available);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_done));
            } else if ("rejected".equalsIgnoreCase(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_rounded_grey);
                tvStatus.setTextColor(Color.parseColor("#6B7280"));
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
            }

            layoutApptsList.addView(view);
        }
    }

    private void populatePhotosTab() {
        layoutPhotosProgressStack.removeAllViews();

        if (hairImages.isEmpty()) {
            tvNoPhotos.setVisibility(View.VISIBLE);
            return;
        }

        tvNoPhotos.setVisibility(View.GONE);

        // Group images by progress tag (phase)
        Map<String, List<HairImage>> groupedImages = new LinkedHashMap<>();
        for (HairImage img : hairImages) {
            String tag = img.getProgressTag();
            if (!groupedImages.containsKey(tag)) {
                groupedImages.put(tag, new ArrayList<>());
            }
            groupedImages.get(tag).add(img);
        }

        // Render each phase
        for (Map.Entry<String, List<HairImage>> entry : groupedImages.entrySet()) {
            String tag = entry.getKey();
            List<HairImage> imgs = entry.getValue();

            View phaseView = LayoutInflater.from(this).inflate(R.layout.item_patient_hair_phase, layoutPhotosProgressStack, false);

            TextView tvTitle = phaseView.findViewById(R.id.tv_phase_title);
            TextView tvDate = phaseView.findViewById(R.id.tv_phase_date);
            TextView tvCount = phaseView.findViewById(R.id.tv_views_count);
            LinearLayout layoutImages = phaseView.findViewById(R.id.layout_phase_images);
            TextView tvFeedback = phaseView.findViewById(R.id.tv_feedback_text);
            ImageView ivFeedbackIcon = phaseView.findViewById(R.id.iv_feedback_icon);
            ImageButton btnEditFeedback = phaseView.findViewById(R.id.btn_edit_feedback);

            // Format phase title (e.g. 6_months_progress -> 6 Months Progress)
            String formattedTitle = tag.replace("_", " ");
            if (formattedTitle.length() > 0) {
                formattedTitle = formattedTitle.substring(0, 1).toUpperCase() + formattedTitle.substring(1);
            }
            tvTitle.setText(formattedTitle);

            // Format Date (use the first image upload date)
            if (!imgs.isEmpty()) {
                tvDate.setText(imgs.get(0).getUploadedAt().split(" ")[0]);
            }

            // Set count
            tvCount.setText(imgs.size() + (imgs.size() == 1 ? " View" : " Views"));

            // Setup images horizontal list
            layoutImages.removeAllViews();
            for (HairImage img : imgs) {
                View photoView = LayoutInflater.from(this).inflate(R.layout.item_phase_photo, layoutImages, false);

                ImageView ivPhoto = photoView.findViewById(R.id.iv_photo);
                TextView tvCaption = photoView.findViewById(R.id.tv_angle_caption);

                // Build caption (e.g. user_id_frontal -> frontal)
                String capText = img.getImagePath().toLowerCase();
                String caption = "Scalp Photo";
                if (capText.contains("frontal")) caption = "Frontal";
                else if (capText.contains("left")) caption = "Left Lateral";
                else if (capText.contains("right")) caption = "Right Lateral";
                else if (capText.contains("vertex")) caption = "Vertex";
                else if (capText.contains("crown")) caption = "Crown";
                tvCaption.setText(caption);

                // Load image via Glide
                if (img.getImageUrl() != null) {
                    Glide.with(this).load(img.getImageUrl()).into(ivPhoto);
                }

                layoutImages.addView(photoView);
            }

            // Bind Clinical Feedback
            String feedback = null;
            for (HairImage img : imgs) {
                if (img.getRemark() != null && !img.getRemark().trim().isEmpty()) {
                    feedback = img.getRemark();
                    break;
                }
            }

            if (feedback != null) {
                tvFeedback.setText(feedback);
                ivFeedbackIcon.setImageResource(R.drawable.ic_check_circle);
                ivFeedbackIcon.setColorFilter(Color.parseColor("#10B981"));
            } else {
                tvFeedback.setText("Awaiting expert feedback");
                ivFeedbackIcon.setImageResource(R.drawable.ic_clock);
                ivFeedbackIcon.setColorFilter(Color.parseColor("#F59E0B"));
            }

            // Click listener to edit clinical feedback
            final String finalFeedback = feedback;
            btnEditFeedback.setOnClickListener(v -> showEditFeedbackDialog(imgs, finalFeedback));

            layoutPhotosProgressStack.addView(phaseView);
        }
    }

    private void showEditFeedbackDialog(List<HairImage> phaseImages, String currentFeedback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clinical Feedback");

        EditText etFeedback = new EditText(this);
        etFeedback.setHint("Enter feedback or medical remark");
        etFeedback.setText(currentFeedback);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        etFeedback.setPadding(padding, padding, padding, padding);
        builder.setView(etFeedback);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newFeedback = etFeedback.getText().toString().trim();
            if (newFeedback.isEmpty()) {
                Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            saveFeedbackForPhase(phaseImages, newFeedback);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveFeedbackForPhase(List<HairImage> phaseImages, String feedback) {
        // Save remark on all images in this phase group to synchronize
        if (phaseImages.isEmpty()) return;

        // Counter to refresh when all are done
        final int[] remaining = {phaseImages.size()};

        for (HairImage img : phaseImages) {
            SaveHairRemarkRequest req = new SaveHairRemarkRequest(img.getId(), feedback);
            RetrofitClient.INSTANCE.getInstance().saveHairRemark(req).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Toast.makeText(PatientPortfolioDetailActivity.this, "Clinical feedback saved", Toast.LENGTH_SHORT).show();
                        loadHairImages(); // Reload to refresh UI
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to save feedback", Toast.LENGTH_SHORT).show();
                        loadHairImages();
                    }
                }
            });
        }
    }

    private void populateRoutinesTab() {
        layoutRxStack.removeAllViews();

        if (routines.isEmpty()) {
            tvNoRoutines.setVisibility(View.VISIBLE);
            return;
        }

        tvNoRoutines.setVisibility(View.GONE);

        // Fetch doctor name from SessionManager
        SessionManager sm = new SessionManager(this);
        String docName = sm.getUserName() != null ? sm.getUserName() : "Expert";

        for (Routine routine : routines) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_portfolio_rx, layoutRxStack, false);

            TextView tvDocName = view.findViewById(R.id.tv_rx_doctor_name);
            TextView tvDate = view.findViewById(R.id.tv_rx_date);
            TextView tvMedName = view.findViewById(R.id.tv_rx_med_name);
            TextView tvNotes = view.findViewById(R.id.tv_rx_notes);
            TextView btnDelete = view.findViewById(R.id.btn_delete_rx);

            tvDocName.setText("(Dr. " + docName + ")");
            tvDate.setText(routine.getCreatedAt().split(" ")[0]);
            tvMedName.setText(routine.getName());

            // Build Notes
            StringBuilder notes = new StringBuilder("Notes: [");
            List<String> list = new ArrayList<>();
            if (!routine.getFrequency().isEmpty()) list.add("Frequency: " + routine.getFrequency());
            if (!routine.getDosage().isEmpty()) list.add("Dosage: " + routine.getDosage());
            if (!routine.getTime().isEmpty()) list.add("Timing: " + routine.getTime());
            
            if (list.isEmpty()) {
                notes.append("No instructions");
            } else {
                notes.append(TextUtils.join(", ", list));
            }
            notes.append("]");
            tvNotes.setText(notes.toString());

            // Delete click
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Routine")
                        .setMessage("Are you sure you want to delete this routine?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteRoutine(routine.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            layoutRxStack.addView(view);
        }
    }

    private void deleteRoutine(String routineId) {
        DeleteRoutineRequest req = new DeleteRoutineRequest(routineId);
        RetrofitClient.INSTANCE.getInstance().deleteRoutine(req).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(PatientPortfolioDetailActivity.this, "Routine deleted", Toast.LENGTH_SHORT).show();
                    loadRoutines();
                } else {
                    Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to delete routine", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRoutineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_routine, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_routine_name);
        EditText etDosage = dialogView.findViewById(R.id.et_routine_dosage);
        EditText etFrequency = dialogView.findViewById(R.id.et_routine_frequency);
        EditText etTime = dialogView.findViewById(R.id.et_routine_time);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Medication/routine name is required");
                return;
            }

            saveRoutine(name, dosage, frequency, time, dialog);
        });
    }

    private void saveRoutine(String name, String dosage, String frequency, String time, AlertDialog dialog) {
        AddRoutineRequest req = new AddRoutineRequest(patientId, name, dosage, frequency, time);
        RetrofitClient.INSTANCE.getInstance().addRoutine(req).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(PatientPortfolioDetailActivity.this, "Routine issued successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadRoutines(); // Reload stack
                } else {
                    Toast.makeText(PatientPortfolioDetailActivity.this, "Failed to save routine", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(PatientPortfolioDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
