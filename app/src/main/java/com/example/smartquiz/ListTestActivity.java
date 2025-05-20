package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Model.Exam;
import com.google.firebase.firestore.DocumentSnapshot;

public class ListTestActivity extends AppCompatActivity {

    //private  Header_Controller headerController;
    private ExamController examController;
    private LinearLayout topicListLayout;
    private static final String TAG = "ListTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        examController = new ExamController();
        topicListLayout = findViewById(R.id.topic_list);

        loadExams();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Header_Controller.setup(this);
    }

    private void loadExams() {
        examController.fetchAllExams((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error fetching exams", error);
                return;
            }
            if (value != null) {
                topicListLayout.removeAllViews();
                Log.d(TAG, "Fetched " + value.size() + " documents.");
                if (value.isEmpty()) {
                    Log.d(TAG, "No exam documents found.");
                }
                for (DocumentSnapshot document : value.getDocuments()) {
                    Exam exam = document.toObject(Exam.class);
                    String examTitle;

                    if (exam != null && exam.getTitle() != null && !exam.getTitle().isEmpty()) {
                        examTitle = exam.getTitle();
                    } else {
                        examTitle = "Unknown Exa title" + document.getId();
                    }

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
                

                    examTextView.setOnClickListener(v -> {
                        Log.d(TAG, "Clicked on exam: " + document.getId() + ", Title: " + examTitle);
                        // TODO: Navigate to exam details activity or start the quiz
                        Intent intent = new Intent(ListTestActivity.this, ExamDetailActivity.class);
                        intent.putExtra("EXAM_ID", document.getId());
                        startActivity(intent);
                    });

                    topicListLayout.addView(examTextView);
                }
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

}
