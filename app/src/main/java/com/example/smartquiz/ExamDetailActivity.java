package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Model.Exam;

public class ExamDetailActivity extends AppCompatActivity {
    private String examId;
    private TextView examTitle;
    private TextView examDuration;
    private TextView examNumberOfQuestion;
    private ImageButton enter;
    private ImageButton cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_infor);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        examTitle = findViewById(R.id.examTitletextView);
        examNumberOfQuestion= findViewById(R.id.examQuestiontextView);
        examDuration= findViewById(R.id.examDurationtextView);
        enter = findViewById(R.id.imageButton5);
        cancel = findViewById(R.id.imageButton6);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //String examId = intent.getStringExtra("EXAM_ID");

        if (extras != null) {
            examId = extras.getString("EXAM_ID");
        }
        loadExamDetails();

        enter.setOnClickListener(v-> {
            Intent newIntent = new Intent(ExamDetailActivity.this, takeTestActivity.class);
            newIntent.putExtra("examId",examId);
            startActivity(newIntent);
        });

        cancel.setOnClickListener(v-> {
            Intent newIntent = new Intent(ExamDetailActivity.this, ListTestActivity.class);
            startActivity(newIntent);
        }) ;
    }

    private void loadExamDetails() {
        ExamController examController = new ExamController();
        examController.fetchQuestionCount(examId, new ExamController.QuestionCountCallback() {
            @Override
            public void onCountFetched(int count) {
                examNumberOfQuestion.setText(String.valueOf(count));
            }
            @Override
            public void onError(Exception e) {
                //caidcmmay
            }
        });
        examController.fetchExamDetails(examId, (value, error) -> {
            if (value != null && value.exists()) {
                Exam exam = value.toObject(Exam.class);
                if (exam != null && exam.getTitle() != null) {
                    int duration = exam.getDuration();
                    examTitle.setText(exam.getTitle());
                    examDuration.setText(String.valueOf(duration));
                }
            }
        });
    }
}