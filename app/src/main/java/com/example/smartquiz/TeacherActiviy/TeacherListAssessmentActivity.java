package com.example.smartquiz.TeacherActiviy;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartquiz.Controller.AssessmentController;
import com.example.smartquiz.Controller.ClassesController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.Model.Assessment;
import com.example.smartquiz.Model.AssessmentSession;
import com.example.smartquiz.Model.Class;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TeacherListAssessmentActivity extends AppCompatActivity {
    TextView class_name;
    LinearLayout ln_assessment_list;
    List<Assessment> assessmentsList;
    TextView emptyStateTextView;
    ImageButton add_btnAssessment;
    TextView textView13;
    String classId;
    final static String TAG ="TeacherListAssessmentActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_list_assessment);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        class_name = findViewById(R.id.class_name);
        ln_assessment_list = findViewById(R.id.scroll_view_assessment_list);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        add_btnAssessment = findViewById(R.id.add_btnAssessment);
        assessmentsList = new ArrayList<>();


        Header_Controller_Teacher.setup(this);
       classId = getIntent().getStringExtra("CLASS_ID");
       ClassesController currClasses = new ClassesController();
       currClasses.getClassDocumentById(classId ,(task)-> {
          if(task.isSuccessful()) {
            DocumentSnapshot classDoc = task.getResult();
            Class currClass = classDoc.toObject(Class.class);
            if (currClass == null) {
              Log.d(TAG, "Class is null");
              return;
            }
            class_name.setText(currClass.getName());
          } else {
                Log.d(TAG, "Class not found");
          }
       });

        AssessmentController assessmentController = new AssessmentController();

        assessmentController.getAssessmentsByClassIdEventLis(classId, (value,error) -> {
            if(value == null || error != null) {return;}
            ln_assessment_list.removeAllViews();
            for (DocumentSnapshot document : value.getDocuments()) {
                Assessment currAssessment = document.toObject(Assessment.class);
                if (currAssessment != null) {
                    emptyStateTextView.setVisibility(View.GONE);
                    currAssessment.setId(document.getId());
                    assessmentsList.add(currAssessment);
                    displayAssessment(currAssessment);
                }
            }
        });

        add_btnAssessment.setOnClickListener(v -> {
            dialog_add_assessment(false,null);
        });
    }

    private void displayAssessment(Assessment assessment) {



        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 32, 16, 8);
        LinearLayout examContainer = new LinearLayout(TeacherListAssessmentActivity.this);
        examContainer.setOrientation(LinearLayout.VERTICAL);
        examContainer.setLayoutParams(params);
        examContainer.setBackgroundResource(R.drawable.blue_box);

        //Create Text view for assessment title
        int paddingPx = dpToPx(16);
        TextView assessmentTextView = new TextView(this);
        assessmentTextView.setText(assessment.getTitle());
        assessmentTextView.setBackgroundResource(R.drawable.light_blue);
        assessmentTextView.setPadding(paddingPx, dpToPx(4), paddingPx, dpToPx(4));
        assessmentTextView.setTypeface(null, Typeface.BOLD_ITALIC);
        assessmentTextView.setBackgroundResource(R.drawable.light_blue);
        assessmentTextView.setTextSize(dpToPx(10));
        assessmentTextView.setTextColor(Color.BLACK);
        assessmentTextView.setElevation(dpToPx(6));
        assessmentTextView.setTranslationZ(dpToPx(6));
        examContainer.addView(assessmentTextView);

        //Create text view to show count of student
        TextView studentCountTextView = new TextView(this);
        studentCountTextView.setText("Số học sinh trong kì thi: " + assessment.getStudent().size());
        studentCountTextView.setTypeface(null, Typeface.BOLD_ITALIC);
        studentCountTextView.setTextSize(dpToPx(6));;
        studentCountTextView.setPadding(paddingPx, 0, paddingPx, 0);
        examContainer.addView(studentCountTextView);

        //Create button to show all student submission
        Button showStudentButton = new Button(this);
        showStudentButton.setText("Xem điểm");
        showStudentButton.setBackgroundResource(R.drawable.light_blue);
        showStudentButton.setBackgroundTintMode(null);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)); // L, T, R, B
        buttonParams.gravity = Gravity.START;
        // Or align to the end (right for LTR languages):
        // buttonParams.gravity = Gravity.END;
        // Or align to the start (left for LTR languages):
        // buttonParams.gravity = Gravity.START;
        showStudentButton.setLayoutParams(buttonParams);
        showStudentButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherListAssessmentActivity.this, TeacherViewStudentScore.class);
            intent.putExtra("ASSESSMENT_ID", assessment.getId());
            startActivity(intent);
        });



        examContainer.addView(showStudentButton);


        ln_assessment_list.addView(examContainer);

        assessmentTextView.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherListAssessmentActivity.this, TeacherAddAssessmentActivity.class);
            intent.putExtra("ASSESSMENT_ID", assessment.getId());
            intent.putExtra("CLASS_ID", classId);
            intent.putExtra("ASSESSMENT_TITLE", assessment.getTitle());
            startActivity(intent);
        });

        assessmentTextView.setOnLongClickListener(v -> {
            dialog_add_assessment(true, assessment);
            return true;
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void dialog_add_assessment(boolean isUpdate, Assessment updateAssessment) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true); // Allow dismissing by tapping outside or back button

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_teacher_create_assessment, null);
        dialog.setContentView(dialogView);
        textView13 = dialogView.findViewById(R.id.textView13);
        TextView class_nameDialog = dialogView.findViewById(R.id.class_name);
        TextView emptyStateTextViewDialog = dialogView.findViewById(R.id.emptyStateTextView);
        EditText AssessmentNameText = dialogView.findViewById(R.id.ExamNameText);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button createAssessmentBtn = dialogView.findViewById(R.id.createAssessmentBtn);

        LinearLayout student_list = dialogView.findViewById(R.id.list_exam_dialog);
        List<Assessment.StudentInfo> studentInfoList = new ArrayList<>();
        List<String> classIdArray = new ArrayList<>();
        classIdArray.add(classId);


        class_nameDialog.setText(class_name.getText());
        cancelBtn.setOnClickListener(view-> {
            dialog.dismiss();
        });

        fetchAndDisplayStudentClass(student_list,emptyStateTextViewDialog,isUpdate,updateAssessment);
        dialog.show();

        if(!isUpdate) {
            createAssessmentBtn.setOnClickListener(view-> {
                getCheckBoxAssessment(student_list,studentInfoList);
                String assessmentName = AssessmentNameText.getText().toString();
                if (studentInfoList.isEmpty() || AssessmentNameText.getText().toString().isEmpty()) {
                    Toast.makeText(TeacherListAssessmentActivity.this, "Thiếu Thông tin!!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Assessment newAssessment = new Assessment();
                newAssessment.setTitle(assessmentName);
                newAssessment.setClassId(Collections.singletonList(classId));
                newAssessment.setStudent(studentInfoList);
                newAssessment.setClassId(classIdArray);

                AssessmentController assessmentController = new AssessmentController();
                assessmentController.createAssessment(newAssessment, (task) -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(TeacherListAssessmentActivity.this, "Assessment created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(TeacherListAssessmentActivity.this, "Failed to create assessment", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            textView13.setText("Cập nhật kì thi");
            createAssessmentBtn.setText("Cập nhật");
            AssessmentNameText.setText(updateAssessment.getTitle());
            createAssessmentBtn.setOnClickListener(view-> {
                getCheckBoxAssessment(student_list,studentInfoList);
                if (studentInfoList.isEmpty() || AssessmentNameText.getText().toString().isEmpty()) {
                    Toast.makeText(TeacherListAssessmentActivity.this, "Thiếu Thông tin!!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateAssessment.setTitle(AssessmentNameText.getText().toString());

                canUpdate(updateAssessment.getId(),studentInfoList, new OnCanUpdateCheckListener() {
                    @Override
                    public void onCheckResult(boolean canUpdate) {
                        if(canUpdate) {
                            AssessmentController assessmentController = new AssessmentController();
                            updateAssessment.setStudent(studentInfoList);
                            assessmentController.updateAssessment(updateAssessment.getId(),updateAssessment, (task) -> {
                                if(task.isSuccessful()) {
                                    Toast.makeText(TeacherListAssessmentActivity.this, "Assessment updated successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(TeacherListAssessmentActivity.this, "Failed to update assessment", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else{
                            Toast.makeText(TeacherListAssessmentActivity.this,"Không thể update vì học sinh vẫn còn trong ca thi!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }

    }

    interface OnCanUpdateCheckListener {
        void onCheckResult(boolean canUpdate);
    }

    private void canUpdate(
            String assessmentId,
            List<Assessment.StudentInfo> newStudentInfoList,
            OnCanUpdateCheckListener listener) {

        AssessmentController assessmentController = new AssessmentController();
        final boolean[] canProceed = {true}; // Use an array for effective finality in lambda

        // 1. Fetch the current assessment details to identify students being removed
        assessmentController.getAssessmentById(assessmentId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Assessment currentAssessment = task.getResult().toObject(Assessment.class);

                List<Assessment.StudentInfo> currentStudents = (currentAssessment != null && currentAssessment.getStudent() != null)
                        ? currentAssessment.getStudent()
                        : new ArrayList<>(); // Handle null case for current students

                // Identify students that are in the *current* list but *not* in the *new* list (i.e., being removed)
                Set<String> newStudentUserIds = newStudentInfoList.stream()
                        .map(Assessment.StudentInfo::getUserId)
                        .collect(Collectors.toSet());

                List<String> removedStudentUserIds = currentStudents.stream()
                        .filter(s -> !newStudentUserIds.contains(s.getUserId()))
                        .map(Assessment.StudentInfo::getUserId)
                        .collect(Collectors.toList());

                    if (removedStudentUserIds.isEmpty()) {
                    // No students are being removed, so the update is safe.
                    listener.onCheckResult(true);
                    return;
                }

                // 2. Check if any of the removed students are present in any session
                assessmentController.getAllSessions(assessmentId, (value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting sessions: " + error.getMessage());
                        listener.onCheckResult(false); // Assume cannot update on error
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            AssessmentSession session = document.toObject(AssessmentSession.class);
                            if (session != null && session.getStudentsInSession() != null) {
                                for (String removedUserId : removedStudentUserIds) {
                                    if (session.getStudentsInSession().contains(removedUserId)) {
                                        // Found a removed student in a session, so cannot proceed
                                        canProceed[0] = false;
                                        break; // No need to check further sessions or students
                                    }
                                }
                                if (!canProceed[0]) {
                                    break; // Break from the session loop if a conflict is found
                                }
                            }
                        }
                    }
                    // Deliver the final result via the callback
                    listener.onCheckResult(canProceed[0]);
                });

            } else {
                Log.e(TAG, "Error getting assessment: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                listener.onCheckResult(false); // Cannot update if assessment details can't be fetched
            }
        });
    }
    private void getCheckBoxAssessment(LinearLayout student_list,
                                       List<Assessment.StudentInfo> studentInfoList) {
        for (int i = 0; i < student_list.getChildCount(); i++) {
            View child = student_list.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    String studentIdExist = (String) checkBox.getTag(R.id.student_uid_tag_key);
                    if (studentIdExist != null) {
                        Assessment.StudentInfo studentInfo = new Assessment.StudentInfo();
                        studentInfo.setUserId(studentIdExist);
                        studentInfo.setSelected(true);
                        studentInfoList.add(studentInfo);
                    } else {
                        String studentId = (String) checkBox.getTag();
                        Assessment.StudentInfo studentInfo = new Assessment.StudentInfo();
                        studentInfo.setUserId(studentId);
                        studentInfo.setSelected(false);
                        studentInfoList.add(studentInfo);}
                }
            }
        }
    }
    
    private void fetchAndDisplayStudentClass(LinearLayout student_list, TextView emptyStateTextViewDialog,
                                             boolean isUpdate, Assessment updateAssessment) {
        student_list.removeAllViews();

        ClassesController classesController = new ClassesController();
        classesController.getClassDocumentById(classId, (task) -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Class currClass = document.toObject(Class.class);
                emptyStateTextViewDialog.setVisibility(View.GONE);
                if (currClass == null || currClass.getStudentsId() == null || currClass.getStudentsId().isEmpty()) {
                    emptyStateTextViewDialog.setText("No students found in this class.");
                    emptyStateTextViewDialog.setVisibility(View.VISIBLE);
                    return;
                }



                List<String> studentIdsFromClass = currClass.getStudentsId();

                LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                checkboxParams.setMargins(16, 8, 16, 8);

                for (String studentId : studentIdsFromClass) {
                    UserController userController = new UserController();
                    userController.getUserIdDocument(studentId, (value, error) -> {
                        if (error != null) {
                            return;
                        }

                        if (value != null && value.exists()) {
                            User student = value.toObject(User.class);
                            if (student != null && student.getName() != null) {
                                CheckBox studentCheckbox = new CheckBox(TeacherListAssessmentActivity.this);

                                studentCheckbox.setText(student.getEmail());
                                studentCheckbox.setTag(student.getUid());
                                if (isUpdate) {
                                    for (Assessment.StudentInfo studentInfo : updateAssessment.getStudent()) {
                                        if (studentInfo.getUserId().equals(student.getUid())) {
                                            studentCheckbox.setChecked(true);
                                            studentCheckbox.setTag(R.id.student_uid_tag_key,student.getUid());
                                            break;
                                        } else {
                                            studentCheckbox.setChecked(false);
                                        }
                                    }
                                } else {
                                    studentCheckbox.setChecked(true);
                                }

                                studentCheckbox.setTextSize(16);
                                studentCheckbox.setTextColor(Color.BLACK);
                                studentCheckbox.setLayoutParams(checkboxParams);

                                student_list.addView(studentCheckbox);
                            }
                        }
                    });
                }

            } else {
                emptyStateTextViewDialog.setText("Failed to load class data.");
                emptyStateTextViewDialog.setVisibility(View.VISIBLE);
            }
        });
    }

    // private void fetchAndDisplayStudentClass(LinearLayout student_list) {
    //     student_list.removeAllViews();
    //     ClassesController classesController = new ClassesController();
    //     classesController.getClassDocumentById(classId, (task)->{
    //         if(task.isSuccessful()) {
    //             emptyStateTextView.setVisibility(View.GONE);
    //             DocumentSnapshot document = task.getResult();
    //             Class currClass = document.toObject(Class.class);
    //             if (currClass == null) {return;}

    //             List<String> studentIdsFromClass;
    //             studentIdsFromClass = currClass.getStudentsId();


    //             RadioGroup studentListRadioGroup = new RadioGroup(TeacherListAssessmentActivity.this);
    //             studentListRadioGroup.setOrientation(LinearLayout.VERTICAL);
    //             LinearLayout.LayoutParams rgParams = new LinearLayout.LayoutParams(
    //                     ViewGroup.LayoutParams.MATCH_PARENT,
    //                     ViewGroup.LayoutParams.WRAP_CONTENT);
    //             rgParams.setMargins(16, 8, 16, 8);
    //             studentListRadioGroup.setLayoutParams(rgParams);

    //             for (String studentId : studentIdsFromClass) {
    //                 UserController userController = new UserController();
    //                 userController.getUserIdDocument(studentId , (value,error) ->{
    //                     if (error !=null) {return;}
    //                     if (value!=null) {
    //                         User student = value.toObject(User.class);
    //                         RadioButton studentRadioButton = new RadioButton(TeacherListAssessmentActivity.this);
    //                         studentRadioButton.setText(student.getName());
    //                         studentRadioButton.setTag(student);
    //                         studentRadioButton.setSelected(true);

    //                         studentRadioButton.setTextSize(16);
    //                         studentRadioButton.setTextColor(Color.BLACK);

    //                         LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(
    //                                 ViewGroup.LayoutParams.MATCH_PARENT,
    //                                 ViewGroup.LayoutParams.WRAP_CONTENT);
    //                         rbParams.setMargins(8, 4, 8, 4);
    //                         studentRadioButton.setLayoutParams(rbParams);
    //                         studentListRadioGroup.addView(studentRadioButton);
    //                     }
    //                 });
    //             }
    //             student_list.addView(studentListRadioGroup);
    //         }
    //     });
    // }
}