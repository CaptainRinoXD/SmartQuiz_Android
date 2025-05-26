package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class resultActivity extends AppCompatActivity {

    private TextView scoreTextView;
    private TextView durationTextView;
    private ImageButton cancel;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        scoreTextView = findViewById(R.id.examQuestiontextView);
        durationTextView = findViewById(R.id.examDurationtextView);
        titleTextView = findViewById(R.id.examTitletextView);
        cancel = findViewById(R.id.imageButton6);


        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int total = intent.getIntExtra("totalQuestions", 0);
        String duration = intent.getStringExtra("examDuration");
        String title = intent.getStringExtra("examTitle");

        scoreTextView.setText(" " + score + "/" + total);
        durationTextView.setText("" + duration);
        titleTextView.setText(" " + title);

        cancel.setOnClickListener(v-> {
            Intent newIntent = new Intent(resultActivity.this, ListTestActivity.class);
            startActivity(newIntent);
        }) ;
    }
}
