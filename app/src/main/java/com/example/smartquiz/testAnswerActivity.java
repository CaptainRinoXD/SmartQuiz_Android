package com.example.smartquiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Question;
import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.Model.SubmissionAnswer;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class testAnswerActivity extends AppCompatActivity {
    private static final String TAG = "testAnswerActivity";

    private String examId;

    private LinearLayout questionsLinearLayout;

    private ExamController examController;
    private List<Question> questionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_answer);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        questionsLinearLayout = findViewById(R.id.questionsLinearLayout);

        questionList = new ArrayList<>();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            examId = extras.getString("examId");
            Log.d(TAG, "Received exam id: " + examId);
            fetchExamQuesion(examId);
            fetchExamDetail(examId);
        } else {
            Log.d(TAG, "No exam id received.");
        }

    }

    private void fetchExamQuesion(String examId) {
        examController = new ExamController();
        examController.fetchExamQuestions(examId, (value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed:", error);
                return;
            }

            questionList.clear();
            if (value != null) {
                for (DocumentSnapshot document : value.getDocuments()) {
                    Question question = document.toObject(Question.class);
                    if (question != null) {
                        question.setId(document.getId());
                        questionList.add(question);
                    }
                }
                loadSubmissionAndDisplayAnswers(); // Sau khi load câu hỏi, hiển thị bài nộp
            }
        });
    }

    private void fetchExamDetail(String examId) {
        examController = new ExamController();
        examController.fetchExamDetails(examId, (value, error) -> {
            if (error != null || value == null) {
                Log.w(TAG, "Listen failed:", error);
                return;
            }
            Exam exam = value.toObject(Exam.class);
            // Hiện chưa cần làm gì thêm với chi tiết bài thi
        });
    }

    private void loadSubmissionAndDisplayAnswers() {
        SubmissionController submissionController = new SubmissionController();
        submissionController.getSubmissionByExamId(examId, submission -> {
            if (submission == null) {
                Toast.makeText(this, "No submission found for this exam", Toast.LENGTH_SHORT).show();
                return;
            }

            List<SubmissionAnswer> submissionAnswers = submission.getAnswers();
            questionsLinearLayout.removeAllViews();

            for (int i = 0; i < questionList.size(); i++) {
                Question question = questionList.get(i);
                SubmissionAnswer subAnswer = null;

                for (SubmissionAnswer answer : submissionAnswers) {
                    if (answer.getQuestionId().equals(question.getId())) {
                        subAnswer = answer;
                        break;
                    }
                }

                if (subAnswer != null) {
                    addAnsweredQuestionToUI(question, i + 1, subAnswer);
                }
            }
        });
    }

    private void addAnsweredQuestionToUI(Question question, int questionNumber, SubmissionAnswer subAnswer) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText("Question " + questionNumber + ": " + question.getQuestionText());
        questionTextView.setTextSize(18);
        questionTextView.setTypeface(null, Typeface.BOLD);
        questionTextView.setTextColor(Color.BLACK);
        questionTextView.setPadding(16, 32, 16, 8);
        questionsLinearLayout.addView(questionTextView);

        LinearLayout answerContainer = new LinearLayout(this);
        answerContainer.setOrientation(LinearLayout.VERTICAL);
        answerContainer.setPadding(32, 0, 32, 16);

        List<String> answers = question.getAnswers();
        int correctIndex = subAnswer.getCorrectAnswerIndex();
        int selectedIndex = subAnswer.getSelectedAnswerIndex();

        for (int i = 0; i < answers.size(); i++) {
            TextView answerText = new TextView(this);
            answerText.setText(answers.get(i));
            answerText.setTextSize(16);
            answerText.setPadding(8, 8, 8, 8);

            if (i == correctIndex) {
                answerText.setBackgroundColor(Color.parseColor("#C8E6C9")); // xanh lá nhạt
            }

            if (selectedIndex == i && selectedIndex != correctIndex) {
                answerText.setBackgroundColor(Color.parseColor("#FFCDD2")); // đỏ nhạt
            }

            answerContainer.addView(answerText);
        }

        questionsLinearLayout.addView(answerContainer);
    }
}
