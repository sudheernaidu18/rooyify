package com.example.rooyify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rooyify.network.QuizReport;
import com.example.rooyify.network.QuizReportsResponse;
import com.example.rooyify.network.RetrofitClient;
import com.example.rooyify.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsListActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private ReportsAdapter adapter;
    private final List<QuizReport> reportsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_list);

        FrameLayout btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvReports = findViewById(R.id.rv_reports_list);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportsAdapter(reportsList);
        rvReports.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.INSTANCE.getInstance().getQuizReports(userId).enqueue(new Callback<QuizReportsResponse>() {
            @Override
            public void onResponse(Call<QuizReportsResponse> call, Response<QuizReportsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        reportsList.clear();
                        List<QuizReport> list = response.body().getReports();
                        if (list != null) {
                            reportsList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();
                        if (reportsList.isEmpty()) {
                            Toast.makeText(ReportsListActivity.this, "No reports found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ReportsListActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReportsListActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuizReportsResponse> call, Throwable t) {
                Toast.makeText(ReportsListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
        private final List<QuizReport> items;

        ReportsAdapter(List<QuizReport> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuizReport item = items.get(position);
            holder.tvTitle.setText("Assessment Report #" + item.getId());
            holder.tvDate.setText("Completed on: " + item.getCreatedAt());

            String risk = item.getRiskLevel();
            holder.tvRiskTag.setText(risk.substring(0, 1).toUpperCase() + risk.substring(1));

            if ("high".equalsIgnoreCase(risk)) {
                holder.tvRiskTag.setBackgroundResource(R.drawable.bg_risk_high);
                holder.tvRiskTag.setTextColor(getResources().getColor(R.color.status_pending)); // Red/Orange tint
            } else if ("moderate".equalsIgnoreCase(risk)) {
                holder.tvRiskTag.setBackgroundResource(R.drawable.bg_risk_moderate);
                holder.tvRiskTag.setTextColor(getResources().getColor(R.color.status_pending));
            } else {
                holder.tvRiskTag.setBackgroundResource(R.drawable.bg_risk_low);
                holder.tvRiskTag.setTextColor(getResources().getColor(R.color.status_done));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ReportsListActivity.this, QuizDetailsActivity.class);
                    intent.putExtra("report_id", item.getId());
                    intent.putExtra("risk_level", item.getRiskLevel());
                    intent.putExtra("answers", item.getAnswers());
                    intent.putExtra("created_at", item.getCreatedAt());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvRiskTag;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_report_title);
                tvDate = itemView.findViewById(R.id.tv_report_date);
                tvRiskTag = itemView.findViewById(R.id.tv_risk_tag);
            }
        }
    }
}