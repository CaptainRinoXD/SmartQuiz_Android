package com.example.smartquiz;

import android.os.Bundle;

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
                        if(value != null && value.exists()) {
                            Exam exam = value.toObject(Exam.class);
                            String examTitle = exam.getTitle();
                        }
                    });
                }
            }
        });
    }
}

