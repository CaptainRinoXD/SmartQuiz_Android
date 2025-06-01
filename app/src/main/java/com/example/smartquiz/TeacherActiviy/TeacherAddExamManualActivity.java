package com.example.smartquiz.TeacherActiviy;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Question;
import com.example.smartquiz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherAddExamManualActivity extends AppCompatActivity implements TeacherUploadDialog.UploadDialogListener {

    private LinearLayout questionContainer;
    private Button addQuestionBtn, uploadExamBtn;
    private ExamController examController = new ExamController();
    private String Assessment_id;
    private String Exam_title;
    private String examId;
    private String created_by;
    private int Duration;
    private List<String> sessionIds;
    private boolean isUpadate = false;

    private List<Question> questionsList = new ArrayList<>();
    private TeacherUploadDialog currentDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_add_exam_manual);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //Header_Controller_Teacher.setup(this);
        Button btn_upload_exam = findViewById(R.id.btn_upload_exam);
        Assessment_id = getIntent().getStringExtra("ASSESSMENT_ID");
        Exam_title = getIntent().getStringExtra("EXAM_TITLE");
        Duration = getIntent().getIntExtra("DURATION", 0);
        sessionIds = getIntent().getStringArrayListExtra("SESSION_LIST");
        try {
            examId = getIntent().getStringExtra("EXAM_ID");
            if(examId != null) {btn_upload_exam.setVisibility(View.GONE);}
            created_by= getIntent().getStringExtra("CREATED_BY");
            if (examId!= null) {isUpadate = true;}
            ExamController examController = new ExamController();
            examController.fetchExamQuestions(examId,(value, error)->{
                if (value == null) {return;}
                for (DocumentSnapshot doc : value) {
                    Question question = doc.toObject(Question.class);
                    List<String> answersList = question.getAnswers();
                    int correctIndex = question.getCorrectAnswerIndex();


                    addQuestionItem();
                    View questionItem = questionContainer.getChildAt(questionContainer.getChildCount() - 1);

                    EditText edtQuestion = questionItem.findViewById(R.id.edt_question_text);
                    EditText edtAnswerA = questionItem.findViewById(R.id.edt_answer_0);
                    EditText edtAnswerB = questionItem.findViewById(R.id.edt_answer_1);
                    EditText edtAnswerC = questionItem.findViewById(R.id.edt_answer_2);
                    EditText edtAnswerD = questionItem.findViewById(R.id.edt_answer_3);
                    RadioGroup radioGroup = questionItem.findViewById(R.id.radio_group_answers);

                    edtQuestion.setText(question.getQuestionText());
                    edtAnswerA.setText(answersList.get(0));
                    edtAnswerB.setText(answersList.get(1));
                    edtAnswerC.setText(answersList.get(2));
                    edtAnswerD.setText(answersList.get(3));

                    if(correctIndex == 0) {
                        radioGroup.check(R.id.radio_answer_0);
                    } else if(correctIndex == 1) {
                        radioGroup.check(R.id.radio_answer_1);
                    } else if (correctIndex == 2) {
                        radioGroup.check(R.id.radio_answer_2);
                    } else if (correctIndex == 3) {
                        radioGroup.check(R.id.radio_answer_3);
                    }

                }

            });
        } catch (Exception e) {}

        btn_upload_exam.setOnClickListener(v -> {
            uploadExamReal();
        });

        questionContainer = findViewById(R.id.question_container);
        addQuestionBtn = findViewById(R.id.btn_add_question);
        uploadExamBtn = findViewById(R.id.btn_submit_exam);

        addQuestionBtn.setOnClickListener(v -> addQuestionItem());
        uploadExamBtn.setOnClickListener(v -> uploadExam());
    }

    private void uploadExamReal() {
        showUploadDialog();
    }

    private void addQuestionItem() {
        Log.d("TeacherAddExam", "addQuestionItem() called");
        View questionItem = LayoutInflater.from(this).inflate(R.layout.question_item, questionContainer, false);
        questionContainer.addView(questionItem);
    }

    private void showUploadDialog() {
        currentDialog = new TeacherUploadDialog(this, this);
        currentDialog.show();
    }


    private void uploadExam() {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < questionContainer.getChildCount(); i++) {
            View qView = questionContainer.getChildAt(i);

            EditText edtQuestion = qView.findViewById(R.id.edt_question_text);
            EditText edtAnswerA = qView.findViewById(R.id.edt_answer_0);
            EditText edtAnswerB = qView.findViewById(R.id.edt_answer_1);
            EditText edtAnswerC = qView.findViewById(R.id.edt_answer_2);
            EditText edtAnswerD = qView.findViewById(R.id.edt_answer_3);
            RadioGroup radioGroup = qView.findViewById(R.id.radio_group_answers);

            String questionText = edtQuestion.getText().toString().trim();
            List<String> answers = new ArrayList<>();
            answers.add(edtAnswerA.getText().toString().trim());
            answers.add(edtAnswerB.getText().toString().trim());
            answers.add(edtAnswerC.getText().toString().trim());
            answers.add(edtAnswerD.getText().toString().trim());

            int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            int correctIndex = -1;
            if (checkedRadioButtonId == R.id.radio_answer_0) {
                correctIndex = 0;
            } else if (checkedRadioButtonId == R.id.radio_answer_1) {
                correctIndex = 1;
            } else if (checkedRadioButtonId == R.id.radio_answer_2) {
                correctIndex = 2;
            } else if (checkedRadioButtonId == R.id.radio_answer_3) {
                correctIndex = 3;
            }

            if (questionText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung câu hỏi " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean emptyAnswerFound = false;
            for (int idx = 0; idx < answers.size(); idx++) {
                if (answers.get(idx).isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đáp án " + (char)('A' + idx) + " cho câu hỏi " + (i + 1), Toast.LENGTH_SHORT).show();
                    emptyAnswerFound = true;
                    break;
                }
            }
            if (emptyAnswerFound) return;

            if (correctIndex == -1) {
                Toast.makeText(this, "Vui lòng chọn đáp án đúng cho câu hỏi " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            questions.add(new Question(questionText, answers, correctIndex));
        }

        // Tạo đối tượng Exam (có thể nhập thêm tiêu đề, thời gian hoặc gán mặc định)
        Exam exam = new Exam();
        exam.setCreatedBy(created_by);
        exam.setTitle(Exam_title);
        exam.setDuration(Duration);
        exam.setAssessment_id(Assessment_id);
        exam.setSessionId(sessionIds);

        if (isUpadate) {
            exam.setId(examId);
            exam.setCreatedBy(created_by);
            examController.updateExam(examId,exam, questions,(task)->{
                if(task.isSuccessful()){
                    Toast.makeText(TeacherAddExamManualActivity.this, "Cập nhật đề thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TeacherAddExamManualActivity.this, "Lỗi khi cập nhật đề", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            examController.createExam(exam, questions, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(TeacherAddExamManualActivity.this, "Tải đề thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(TeacherAddExamManualActivity.this, "Lỗi khi tải đề", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    public void onUploadSuccess(List<Question> questions, String message) {
        runOnUiThread(() -> {
            this.questionsList = questions;
            displayQuestions();
            Toast.makeText(this, message + "\nLoaded " + questions.size() + " questions",
                    Toast.LENGTH_LONG).show();
            Log.d("TeacherMain", "Successfully loaded " + questions.size() + " questions");
        });
    }

    private void displayQuestions(){
        for (Question question : questionsList) {
            List<String> answersList = question.getAnswers();
            int correctIndex = question.getCorrectAnswerIndex();


            addQuestionItem();
            View questionItem = questionContainer.getChildAt(questionContainer.getChildCount() - 1);

            EditText edtQuestion = questionItem.findViewById(R.id.edt_question_text);
            EditText edtAnswerA = questionItem.findViewById(R.id.edt_answer_0);
            EditText edtAnswerB = questionItem.findViewById(R.id.edt_answer_1);
            EditText edtAnswerC = questionItem.findViewById(R.id.edt_answer_2);
            EditText edtAnswerD = questionItem.findViewById(R.id.edt_answer_3);
            RadioGroup radioGroup = questionItem.findViewById(R.id.radio_group_answers);

            edtQuestion.setText(question.getQuestionText());
            edtAnswerA.setText(answersList.get(0));
            edtAnswerB.setText(answersList.get(1));
            edtAnswerC.setText(answersList.get(2));
            edtAnswerD.setText(answersList.get(3));

            if(correctIndex == 0) {
                radioGroup.check(R.id.radio_answer_0);
            } else if(correctIndex == 1) {
                radioGroup.check(R.id.radio_answer_1);
            } else if (correctIndex == 2) {
                radioGroup.check(R.id.radio_answer_2);
            } else if (correctIndex == 3) {
                radioGroup.check(R.id.radio_answer_3);
            }
        }
    }

    @Override
    public void onUploadError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e("TeacherMain", "Upload error: " + errorMessage);
        });
    }

    @Override
    public void onCancelClicked() {
        Log.d("TeacherMain", "Upload cancelled");
    }

    @Override
    public void onFileSelectionRequested() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter for Word documents
        String[] mimeTypes = {
                "application/msword", // .doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        selectFileLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> selectFileLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedFileUri = result.getData().getData();
                if (currentDialog != null && selectedFileUri != null) {
                    currentDialog.onFileSelected(selectedFileUri);
                }
            }
        });



}
