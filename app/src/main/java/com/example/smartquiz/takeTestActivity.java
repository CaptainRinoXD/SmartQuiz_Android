package com.example.smartquiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.example.smartquiz.Util.LoadingDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class takeTestActivity extends AppCompatActivity {
    private static final String TAG = "takeTestActivity";

    private String examId;
    private TextView textViewDuration;
    private LinearLayout questionsLinearLayout;
    private ImageButton submitButton;
    private ExamController examController;
    private List<Question> questionList;
    private LoadingDialogFragment loadingDialog;
    private int durationInMinutes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_take_test);
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
        textViewDuration = findViewById(R.id.duration);
        submitButton = findViewById(R.id.imageButton7);
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

        submitButton.setOnClickListener(view -> {
            showLoading(true);
            submissionExam(examId);
        });
    }

    private void fetchExamQuesion(String examId) {
        examController = new ExamController();
        examController.fetchExamQuestions(examId, (value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed:", error);
                return;
            }

            Log.d(TAG, "Fetched " + value.size() + " documents.");
            if (value.isEmpty()) {
                Log.d(TAG, "No question documents found.");
            }

            questionList.clear();
            questionsLinearLayout.removeAllViews();
            if (value != null) {
                for (DocumentSnapshot document : value.getDocuments()) {
                    Question question = document.toObject(Question.class);
                    if (question != null) {
                        questionList.add(question);
                        String questionId = document.getId();
                        question.setId(questionId);
                        addQuestionToUI(question, questionList.size());
                    }
                }
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
            if (exam != null) {
                durationInMinutes = exam.getDuration();
                int durationInMillis = durationInMinutes * 60 * 1000;
                timerDuration(durationInMillis, examId);
            }
        });
    }

    private void timerDuration(int durationInMillis , String examId) {
        new CountDownTimer(durationInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                String formattedTime = String.format("%02d:%02d", minutes, seconds);
                textViewDuration.setText(formattedTime);
            }
            public void onFinish() {
                textViewDuration.setText("Finish");
                submissionExam(examId);
            }
        }.start();
    }

    private void submissionExam(String examId ) {
        Submission submission = new Submission();
        submission.setExamId(examId);
        List<SubmissionAnswer> answers = new ArrayList<>();

        for (int i = 0; i < questionsLinearLayout.getChildCount(); i++) {
            View child = questionsLinearLayout.getChildAt(i);

            if (child instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) child;
                String questionId = (String) radioGroup.getTag(); //get question id from tag
                Log.d(TAG, "Question ID from radioTag: " + questionId);
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    SubmissionAnswer submissionAnswer = new SubmissionAnswer();
                    submissionAnswer.setQuestionId(questionId); //setting the same id for subAnswers
                    
                    //get the original index for the selected answer
                    int selectedAnswerOriginalIndex = (int) selectedRadioButton.getTag();
                    submissionAnswer.setSelectedAnswerIndex(selectedAnswerOriginalIndex);
                    answers.add(submissionAnswer);

                } else {
                    SubmissionAnswer submissionAnswer = new SubmissionAnswer(); 
                    submissionAnswer.setQuestionId(questionId);
                    submissionAnswer.setSelectedAnswerIndex(-1); // Indicate no answer selected
                    answers.add(submissionAnswer);
                }

            }
        }
        submission.setAnswers(answers);
        String durationString = textViewDuration.getText().toString();
        if (!durationString.equals("Finish")) {
            durationInMinutes = durationInMinutes - Integer.parseInt(durationString.split(":")[0]);
            //int durationInMillis = durationInMinutes * 60 * 1000;
        }

        SubmissionController submissionController = new SubmissionController();
        submissionController.submitExam(submission, durationInMinutes, task -> {
            if (task.isSuccessful()) {
                showLoading(false);
                Log.d(TAG, "Exam submitted successfully");
                Toast.makeText(takeTestActivity.this, "Exam submitted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(takeTestActivity.this, ResultActivity.class);
                intent.putExtra("examId", submission.getExamId());
                startActivity(intent);
                finish();
            } else {
                Log.w(TAG, "Error submitting exam", task.getException());
                Toast.makeText(takeTestActivity.this, "Error submitting exam: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addQuestionToUI(Question question, int questionNumber) {
        // Create TextView for the question number and text
        TextView questionTextView = new TextView(this);
        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        questionParams.setMargins(16, 32, 16, 8);
        questionTextView.setLayoutParams(questionParams);
        questionTextView.setText("Question " + questionNumber + ": " + question.getQuestionText());
        questionTextView.setTextSize(18);
        questionTextView.setTypeface(null, Typeface.BOLD);
        questionTextView.setTextColor(Color.BLACK);

        questionsLinearLayout.addView(questionTextView);


        RadioGroup answerRadioGroup = new RadioGroup(this);
        // set tag for each question to setId for answerSubmission (same id for evaluation) check SubmissionController for more infor
        answerRadioGroup.setTag(question.getId());

        LinearLayout.LayoutParams radioGroupParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        radioGroupParams.setMargins(16, 0, 16, 16);
        answerRadioGroup.setLayoutParams(radioGroupParams);
        answerRadioGroup.setOrientation(LinearLayout.VERTICAL);

        // Add RadioButtons for each answer
        List<String> answers = question.getAnswers();
        if (answers != null && !answers.isEmpty()) {
            {
                {
                    // Create a list of indices [0, 1, 2, ..., answers.size() - 1]
                    List<Integer> answerIndices = new ArrayList<>();
                    for (int i = 0; i < answers.size(); i++) {
                        answerIndices.add(i);
                    }

                    // Shuffle the list of indices
                    java.util.Collections.shuffle(answerIndices);
                    Log.d(TAG,"Shuffled Indices: " + answerIndices.toString());
                    // Iterate through the shuffled indices to display answers
                    for (int shuffledIndex : answerIndices) {
                        RadioButton answerRadioButton = new RadioButton(this);
                        LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        answerParams.setMargins(8, 8, 8, 8);
                        answerRadioButton.setLayoutParams(answerParams);
                        // Get the answer text using the shuffled index
                        answerRadioButton.setText(answers.get(shuffledIndex));
                        answerRadioButton.setTextSize(16);
                        answerRadioButton.setTextColor(Color.BLACK); // Or your desired color

                        // Store the ORIGINAL index as the tag
                        answerRadioButton.setTag(shuffledIndex);

                        answerRadioGroup.addView(answerRadioButton);
                    }
                }
            }
        }

        questionsLinearLayout.addView(answerRadioGroup);
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null || !loadingDialog.isVisible()) {
                loadingDialog = LoadingDialogFragment.newInstance();
                // Ensure LoadingDialogFragment.TAG is defined in LoadingDialogFragment
                loadingDialog.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
                setUiEnabled(false);
            }

        } else {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismissAllowingStateLoss();
            }
            loadingDialog = null; // Clear reference
            setUiEnabled(true);
        }
    }

    private void setUiEnabled(boolean enabled) {
        submitButton.setEnabled(enabled);
        for (int i = 0; i < questionsLinearLayout.getChildCount(); i++) {
            questionsLinearLayout.getChildAt(i).setEnabled(enabled);
        }
    }

}