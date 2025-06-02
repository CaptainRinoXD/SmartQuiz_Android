package com.example.smartquiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.Controller.AssessmentController;
import com.example.smartquiz.Controller.ClassesController;
import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Model.Assessment;
import com.example.smartquiz.Model.Class;
import com.example.smartquiz.Model.AssessmentSession;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.TeacherActiviy.TeacherAddAssessmentActivity;
import com.example.smartquiz.Util.LoadingDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ListTestActivity extends AppCompatActivity {

    //private  Header_Controller headerController;
    private TextView emptyStateTextView;
    private LoadingDialogFragment loadingDialog;
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

        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        examController = new ExamController();
        topicListLayout = findViewById(R.id.topic_list);
        showLoading(true);
        //loadExams();
        //loadExamsWithAssessment();
        loadExamsWithAssessmentUpdate();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Header_Controller.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void loadExamsWithAssessmentUpdate() {
        examController.fetchSessionsForCurrentStudent((sessions, error) -> {
            if (error != null) {
                Log.e(TAG, "Error fetching sessions", error);
                return;
            }

            if(sessions.isEmpty()){
                showLoading(false);
            }
            Log.d(TAG, "Fetched " + sessions.size() + " sessions.");
            if (sessions != null) {
                for (AssessmentSession session : sessions) {
                    LinearLayout examContainer = new LinearLayout(ListTestActivity.this);
                    examContainer.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    containerParams.setMargins(16, 32, 16, 8);
                    examContainer.setLayoutParams(containerParams);
                    //so sánh thời gian
                    Date sessionDate = session.getTime();
                    Date now = new Date();
                    if(now.after(sessionDate)){
                        long diffInMillis = now.getTime() - sessionDate.getTime();
                        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                        Log.d("SessionCheck", "Thời gian cách biệt: "+ diffInMinutes +" phút");
                        if (diffInMinutes <= 60) {
                            // Đang trong giờ thi
                            Log.d("SessionCheck", "OK: Đã quá giờ nhưng vẫn trong giới hạn 30 phút.");

                            //Kiểm tra trước khi hiển thị xem học sinh đã làm bài này chưa
                            checkIfStudentHasDoneAssessment(session, hasDone -> {
                                if (hasDone) {
                                    Log.d(TAG, "Student has already done this assessment");
                                    showLoading(false);
                                } else {
                                    ExamController examController = new ExamController();
                                    examController.fetchExamsBySessionId(session.getId(), (exams, errorSEC)->{
                                        if(errorSEC !=null) {Log.d(TAG,"errorSEC:"+ errorSEC);return;}
                                        if(exams!=null) {
                                            emptyStateTextView.setVisibility(View.GONE);
                                            // Lấy ngẫu nhiên một đề thi từ danh sách
                                            List<Exam> examList = new ArrayList<>(exams);
                                            Exam randomExam;
                                            if (!examList.isEmpty()) {
                                                Random random = new Random();
                                                int randomIndex = random.nextInt(examList.size());
                                                randomExam = examList.get(randomIndex);

                                                Log.d("RandomExam", "Mã đề thi ngẫu nhiên: " + randomExam.getTitle());
                                            } else {
                                                randomExam = null;
                                            }

                                            AssessmentController assessmentController = new AssessmentController();
                                            assessmentController.getAssessmentById(session.getAssessmentId(), (task)->{
                                                showLoading(false);
                                                if (task.isSuccessful()) {
                                                    Assessment assessment = task.getResult().toObject(Assessment.class);
                                                    ClassesController classesController = new ClassesController();
                                                    classesController.getClassDocumentById(assessment.getClassId().get(0), (task1)->{
                                                        if (task1.isSuccessful()) {
                                                            Class classes = task1.getResult().toObject(Class.class);

                                                            // Create ClassTextView
                                                            TextView ClassTextView = new TextView(this);
                                                            ClassTextView.setText("Lớp học: "+classes.getName());
                                                            ClassTextView.setBackgroundResource(R.drawable.blue_box);
                                                            int paddingPx = dpToPx(16);
                                                            ClassTextView.setPadding(paddingPx,dpToPx(5),paddingPx,dpToPx(5));
                                                            ClassTextView.setTextSize(20);
                                                            ClassTextView.setTypeface(null, Typeface.ITALIC);
                                                            ClassTextView.setTextColor(Color.BLACK);
                                                            ClassTextView.setElevation(dpToPx(6));
                                                            ClassTextView.setTranslationZ(dpToPx(6));

                                                            // Create ExamTextView
                                                            TextView ExamTextView = new TextView(this);
                                                            ExamTextView.setText("Kì thi đang diễn diễn ra: "+ assessment.getTitle() + ". Bấm vào để bắt đầu bài thi.");
                                                            ExamTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

                                                            // Set container background
                                                            examContainer.setBackgroundResource(R.drawable.white_box);

                                                            // Add views in the correct order
                                                            examContainer.addView(ClassTextView);  // First
                                                            examContainer.addView(ExamTextView); // Second

                                                            topicListLayout.addView(examContainer);


                                                            examContainer.setOnClickListener(v -> {
                                                                Log.d(TAG, "Clicked on exam: " + randomExam.getId() + ", Title: " + randomExam.getTitle());
                                                                // TODO: Navigate to exam details activity or start the quiz
                                                                Intent intent = new Intent(ListTestActivity.this, ExamDetailActivity.class);
                                                                intent.putExtra("EXAM_ID", randomExam.getId());
                                                                intent.putExtra("ASSESSMENT_ID", randomExam.getAssessment_id());
                                                                startActivity(intent);
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                        } else {
                            showLoading(false);
                            // Quá giờ thi
                            Log.d("SessionCheck", "Quá hạn 30 phút.");
                        }
                    } else {
                        showLoading(false);
                        // Chưa tới giờ thi
                        Log.d("SessionCheck", "Chưa tới giờ.");

                        AssessmentController assessmentController = new AssessmentController();
                        assessmentController.getAssessmentById(session.getAssessmentId(), (task)->{
                            Log.d(TAG,"fetch: "+ task.getResult());
                            if (task.isSuccessful()) {
                                Assessment assessment = task.getResult().toObject(Assessment.class);
                                ClassesController classesController = new ClassesController();
                                classesController.getClassDocumentById(assessment.getClassId().get(0), (task1)->{
                                    if (task1.isSuccessful()) {
                                        Class classes = task1.getResult().toObject(Class.class);

                                        // Create ClassTextView
                                        TextView ClassTextView = new TextView(this);
                                        ClassTextView.setText("Lớp học: "+classes.getName());
                                        ClassTextView.setBackgroundResource(R.drawable.blue_box);
                                        int paddingPx = dpToPx(16);
                                        ClassTextView.setPadding(paddingPx,dpToPx(5),paddingPx,dpToPx(5));
                                        ClassTextView.setTextSize(20);
                                        ClassTextView.setTypeface(null, Typeface.ITALIC);
                                        ClassTextView.setTextColor(Color.BLACK);
                                        ClassTextView.setElevation(dpToPx(6));
                                        ClassTextView.setTranslationZ(dpToPx(6));

                                        // Create SessionTextView
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM, yyyy, HH:mm", new Locale("vi", "VN"));
                                        String formattedDate = formatter.format(session.getTime());
                                        TextView SessionTextView = new TextView(this);
                                        SessionTextView.setText("Kì thi sắp diễn ra: "+ assessment.getTitle()+" vào lúc " + formattedDate);
                                        SessionTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

                                        // Set container background
                                        examContainer.setBackgroundResource(R.drawable.white_box);

                                        // Add views in the correct order
                                        examContainer.addView(ClassTextView);  // First
                                        examContainer.addView(SessionTextView); // Second
                                        topicListLayout.addView(examContainer);
                                    }
                                });
                            }
                        });
                    }
                }

            }
        });
    }

    public interface AssessmentCheckCallback {
        void onResult(boolean hasDone);
    }

    private void checkIfStudentHasDoneAssessment(AssessmentSession session, AssessmentCheckCallback callback) {
        SubmissionController submissionController = new SubmissionController();
        submissionController.getAllSubmissionsFromStudent(submissions -> {
            if(submissions == null){
                Log.d(TAG, "Error fetching submissions" + session.getAssessmentId());
                return;
            }
            for (Submission submission : submissions) {
                if (submission.getAssessmentId().equals(session.getAssessmentId())) {
                    callback.onResult(true);
                    return;
                }
            }
            callback.onResult(false);
        });
    }

    private void loadExamsWithAssessment() {
        examController.fetchExamsForCurrentStudent((exams,error) -> {
            if(error !=null) {
                Log.e(TAG, "Error fetching exams", error);
                return;
            }

            if(exams!=null) {
                topicListLayout.removeAllViews();
                for (Exam exam : exams) {
                    String examTitle;
                    if (exam != null && exam.getTitle() != null && !exam.getTitle().isEmpty()) {
                        examTitle = exam.getTitle();
                    } else {
                        examTitle = "Unknown Exa title";
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
                    topicListLayout.addView(examTextView);

                    examTextView.setOnClickListener(v -> {
                        Log.d(TAG, "Clicked on exam: " + exam.getId() + ", Title: " + examTitle);
                        // TODO: Navigate to exam details activity or start the quiz
                        Intent intent = new Intent(ListTestActivity.this, ExamDetailActivity.class);
                        intent.putExtra("EXAM_ID", exam.getId());
                        intent.putExtra("ASSESSMENT_ID", exam.getAssessment_id());
                        startActivity(intent);
                    });
                }
            }
        });
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

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null || !loadingDialog.isVisible()) {
                loadingDialog = LoadingDialogFragment.newInstance();
                // Ensure LoadingDialogFragment.TAG is defined in LoadingDialogFragment
                loadingDialog.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
            }

        } else {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismissAllowingStateLoss();
            }
            loadingDialog = null; // Clear reference

        }
    }

}
