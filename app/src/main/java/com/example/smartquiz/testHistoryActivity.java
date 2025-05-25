package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;

public class testHistoryActivity extends AppCompatActivity {
    private LinearLayout topicListLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_history);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Header_Controller.setup(this);
        topicListLayout = findViewById(R.id.topic_list);
        fetchUserSubmission();

    }

    private void fetchUserSubmission() {
        SubmissionController sbController = new SubmissionController();
        sbController.getAllSubmissionsFromStudent(submissions -> {
            if (submissions != null) {
                for (Submission submission : submissions) {
                    String examId = submission.getExamId();
                    ExamController examController = new ExamController();
                    examController.fetchExamDetails(examId, (value, error) -> {
                        if (value != null && value.exists()) {
                            Exam exam = value.toObject(Exam.class);
                            String examTitle = exam.getTitle();

                            TextView examTextView = new TextView(this);
                            examTextView.setText(examTitle);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            int marginVerticalPx = dpToPx(4);
                            params.setMargins(0, marginVerticalPx, 0, marginVerticalPx);
                            examTextView.setLayoutParams(params);

                            examTextView.setBackgroundResource(R.drawable.white_box);
                            int paddingPx = dpToPx(16);
                            examTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

                            // 👇 THÊM: bắt sự kiện click để chuyển sang TestAnswerActivity
                            examTextView.setOnClickListener(v -> {
                                Intent intent = new Intent(testHistoryActivity.this, testAnswerActivity.class);
                                intent.putExtra("examId", examId);
                                startActivity(intent);
                            });

                            topicListLayout.addView(examTextView);
                        }
                    });
                }
            }
        });
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

}

