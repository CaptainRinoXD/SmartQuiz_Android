package com.example.smartquiz.TeacherActiviy;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.R;

public class TeacherViewStudentScore extends AppCompatActivity {
    private LinearLayout list_score_LN;
    private static final String TAG = "TeacherViewStudentScore";
    private TextView emptyStateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_view_student_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        String assessmentId = getIntent().getStringExtra("ASSESSMENT_ID");
        list_score_LN = findViewById(R.id.list_score_LN);
        SubmissionController submissionController = new SubmissionController();
        submissionController.getSubmissionByAssessmentId(assessmentId,(submissions) -> {
            if(submissions != null) {
                Log.d(TAG, "Number of submissions: " + submissions.size());
                list_score_LN.removeAllViews();
                emptyStateTextView.setVisibility(View.GONE);
                for (Submission submission : submissions) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(16, 32, 16, 8);

                    TextView studentScore = new TextView(this);
                    studentScore.setLayoutParams(params);
                    studentScore.setTypeface(null, Typeface.ITALIC);
                    studentScore.setBackgroundResource(R.drawable.light_blue);
                    studentScore.setTextSize(dpToPx(8));
                    studentScore.setTextColor(Color.BLACK);
                    studentScore.setElevation(dpToPx(6));
                    studentScore.setTranslationZ(dpToPx(6));
                    studentScore.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

                    UserController userController = new UserController();
                    userController.getUserIdDocument(submission.getStudentId(), (value,error) -> {
                        if(value == null || error != null) {return;}
                        User user = value.toObject(User.class);
                        studentScore.setText( user.getName()+ " có điểm là: " + submission.getScore());
                        list_score_LN.addView(studentScore);
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