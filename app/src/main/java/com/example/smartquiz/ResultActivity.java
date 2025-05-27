package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartquiz.Controller.SubmissionController;
import com.google.firebase.firestore.FirebaseFirestore;



public class ResultActivity extends AppCompatActivity {  // 🔧 PHẢI extends AppCompatActivity
    private ImageButton cancel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {  // 🔧 onCreate cần @Nullable
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView titleTextView = findViewById(R.id.examTitletextView);
        TextView scoreTextView = findViewById(R.id.examQuestiontextView);
        TextView durationTextView = findViewById(R.id.examDurationtextView);
        cancel = findViewById(R.id.imageButton6);

        cancel.setOnClickListener(v-> {
            Intent newIntent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(newIntent);
        }) ;


        String examId = getIntent().getStringExtra("examId");  // ✅ OK nếu đã truyền examId

        SubmissionController submissionController = new SubmissionController();
        submissionController.getStudentSubmission(examId, submission -> {
            if (submission != null) {
                scoreTextView.setText(String.format("%.1f / 20", submission.getScore()));
                durationTextView.setText(formatDuration(submission.getDuration()));

                // Lấy tên đề thi từ Firestore
                FirebaseFirestore.getInstance()
                        .collection("exams")
                        .document(examId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String title = documentSnapshot.getString("title");
                            titleTextView.setText(title != null ? title : "Không có tên đề");
                        });
            } else {
                scoreTextView.setText("Không tìm thấy kết quả");
                durationTextView.setText("N/A");
                titleTextView.setText("N/A");
            }
        });
    }

    // ✅ Hàm định dạng thời gian từ giây sang phút:giây
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d phút %02d giây", minutes, seconds);
    }
}
