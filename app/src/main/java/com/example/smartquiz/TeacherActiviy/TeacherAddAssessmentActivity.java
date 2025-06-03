package com.example.smartquiz.TeacherActiviy;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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
import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.Model.Assessment;
import com.example.smartquiz.Model.AssessmentSession;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TeacherAddAssessmentActivity extends AppCompatActivity {
    private final static String TAG ="TeacherAddAssessmentActivity";
    ImageButton add_session;
    LinearLayout list_session_ln;
    String Assessment_id;
    LinearLayout list_exam_ln;

    TextView emptyStateTextView1;
    TextView emptyStateTextView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_add_session_exam);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Header_Controller_Teacher.setup(this);

        Assessment_id = getIntent().getStringExtra("ASSESSMENT_ID");
        TextView assessment_name = findViewById(R.id.assessment_name);
        ImageButton add_exam = findViewById(R.id.add_btnExam);
        add_session = findViewById(R.id.add_btnSession);
        list_session_ln = findViewById(R.id.session_list);
        list_exam_ln = findViewById(R.id.list_exam_dialog);
        emptyStateTextView1 = findViewById(R.id.emptyStateTextView1);
        emptyStateTextView2 = findViewById(R.id.emptyStateTextView);

        fetchSessionExamToScrollView();
        assessment_name.setText("Kì thi: "+getIntent().getStringExtra("ASSESSMENT_TITLE"));

        add_session.setOnClickListener(v -> {
            dialog_add_session(false, null);
        });

        add_exam.setOnClickListener(v -> {
            AssessmentController assessmentController = new AssessmentController();
            assessmentController.getAllSessions(Assessment_id,(value,error)->{
                if (value != null){
                    dialog_add_exam(false, null);
                } else {
                    Toast.makeText(TeacherAddAssessmentActivity.this, "Hãy thêm ca thi để tạo đề.", Toast.LENGTH_SHORT).show();

                }
            });

        });


    }

    private void dialog_add_session(boolean isUpdate, String sessionId) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_teacher_assessment_session, null);
        dialog.setContentView(dialogView);


        TextView emptyStateTextView = dialogView.findViewById(R.id.emptyStateTextView);
        LinearLayout session_LineraLay_dialog = dialogView.findViewById(R.id.list_exam_dialog);
        TextView SessionNameText = dialogView.findViewById(R.id.ExamNameText);
        Button submit_create_SessionBtn = dialogView.findViewById(R.id.submit_create_SessionBtn2);
        Button randomBtn = dialogView.findViewById(R.id.randomBtn);

        TextView textViewSelectDateTime = dialogView.findViewById(R.id.textViewSelectDateTime);

        List<String> studentInSession = new ArrayList<>();
        List<CheckBox> studentCheckboxes = new ArrayList<>();


        //time picker logic
        final int[] dialogSelectedYear = {0};
        final int[] dialogSelectedMonth = {0};
        final int[] dialogSelectedDay = {0};
        final int[] dialogSelectedHour = {0};
        final int[] dialogSelectedMinute = {0};
        textViewSelectDateTime.setOnClickListener(v ->
                showDateTimePicker(textViewSelectDateTime, dialogSelectedYear, dialogSelectedMonth, dialogSelectedDay, dialogSelectedHour, dialogSelectedMinute)
        );

        //random checkbox logic
        randomBtn.setOnClickListener(v -> {
            if (studentCheckboxes.isEmpty()) {
                Toast.makeText(this, "No students available to select randomly", Toast.LENGTH_SHORT).show();
                return;
            }

            for (CheckBox cb : studentCheckboxes) {
                cb.setChecked(false);
            }

            Collections.shuffle(studentCheckboxes);

            int totalStudents = studentCheckboxes.size();
            if (totalStudents == 0) return;

            int maxToSelect = Math.min(totalStudents / 2, 10);
            if (maxToSelect < 1) maxToSelect = 1;

            Random random = new Random();
            int numToSelect = random.nextInt(maxToSelect) + 1;

            for (int i = 0; i < numToSelect; i++) {
                studentCheckboxes.get(i).setChecked(true);
            }
        });


        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        checkboxParams.setMargins(16, 8, 16, 8);


        if(!isUpdate){
            AssessmentController assessmentController = new AssessmentController();
            //Display list of students from Assessment
            assessmentController.listenForUnselectedStudents(Assessment_id,false,(valueMain,
                                                                            errorMain)->{
                if (valueMain != null){
                    session_LineraLay_dialog.removeAllViews();
                    emptyStateTextView.setVisibility(View.GONE);
                    for(Assessment.StudentInfo studentInfo :valueMain ) {
                        CheckBox studentCheckbox = new CheckBox(TeacherAddAssessmentActivity.this);
                        UserController userController = new UserController();
                        userController.getUserIdDocument(studentInfo.getUserId(),(value, error)->{
                            if (value != null){
                                User student = value.toObject(User.class);
                                studentCheckbox.setText(student.getEmail());
                                studentCheckbox.setTag(student.getUid());
                                studentCheckbox.setTextSize(16);
                                studentCheckbox.setTextColor(Color.BLACK);
                                studentCheckbox.setLayoutParams(checkboxParams);
                                session_LineraLay_dialog.addView(studentCheckbox);

                                //For random checkbox
                                studentCheckboxes.add(studentCheckbox);
                            }
                        });
                    }
                }
            });

            //submitting to create new session
            submit_create_SessionBtn.setOnClickListener(view ->{
                getCheckBoxAssessment(session_LineraLay_dialog,studentInSession,null,false);
                String sessionName = SessionNameText.getText().toString();

                if (sessionName.isEmpty() || studentInSession.isEmpty() || dialogSelectedYear[0] == 0) {
                    Toast.makeText(TeacherAddAssessmentActivity.this, "Please complete the form (Name, Students, Date, and Time)!", Toast.LENGTH_SHORT).show();
                    studentInSession.clear();
                    return;
                }
                AssessmentSession newSession = new AssessmentSession();
                newSession.setTitle(sessionName);
                newSession.setStudentsInSession(studentInSession);




                //time picker
                Calendar calendar = Calendar.getInstance();
                calendar.set(dialogSelectedYear[0], dialogSelectedMonth[0], dialogSelectedDay[0], dialogSelectedHour[0], dialogSelectedMinute[0], 0);
                Date selectedDateTime = calendar.getTime();

                Date now = new Date();
                if(selectedDateTime.after(now)) {
                    long diffInMillis = selectedDateTime.getTime() - now.getTime();
                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                    Log.d("SessionCheck", "Thời gian cách biệt: " + diffInMinutes + " phút");
                    if(diffInMinutes <= 30) {
                        Toast.makeText(TeacherAddAssessmentActivity.this, "Không thể chọn ngày giờ quá gần giờ hiện tại", Toast.LENGTH_SHORT).show();
                        return;
                    } else if(diffInMinutes > 30) {
                        newSession.setTime(selectedDateTime);
                    }
                } else {
                    Toast.makeText(TeacherAddAssessmentActivity.this, "Không thể chọn ngày quá giờ trong quá khứ", Toast.LENGTH_SHORT).show();
                    return;
                }

                //logic for setting student isSelected
                for (String studentId : studentInSession) {
                    assessmentController.updateStudentSelection(Assessment_id, studentId, true, (task) -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Student selection updated successfully");
                        } else {
                            Log.w(TAG, "Error updating student selection");
                        }
                    });
                }



                assessmentController.addSession(Assessment_id,newSession,(documentReference)->{
                            Toast.makeText(TeacherAddAssessmentActivity.this, "Tạo ca thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }, exception -> {
                            Log.w(TAG, "Error adding session",exception);
                            Toast.makeText(this, "Failed to add session: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                );
                dialog.dismiss();
            });
        } else {
            AssessmentController assessmentController = new AssessmentController();
            List<String> StudentToRemoveFromSession = new ArrayList<>();
            List<String> StudentAlreadyInSession = new ArrayList<>();
            List<String> allStudent = new ArrayList<>();
            AtomicReference<AssessmentSession> updateSession = new AtomicReference<>(new AssessmentSession());

            //Get all student in assessment
            allStudent.clear();
            assessmentController.listenForUnselectedStudents(Assessment_id, false, (valueSec, errorSec) -> {
                for (Assessment.StudentInfo studentInfo : valueSec) {
                    allStudent.add(studentInfo.getUserId());
                }
            });
            
            //Display information from selected session
            assessmentController.getSessionsById(Assessment_id,sessionId,(valueMain, errorMain)->{
                AssessmentSession assessmentSession = valueMain.toObject(AssessmentSession.class);
                if (assessmentSession == null) {return;}

                updateSession.set(assessmentSession);
                SessionNameText.setText(assessmentSession.getTitle());
                allStudent.addAll(assessmentSession.getStudentsInSession());
                StudentAlreadyInSession.addAll(assessmentSession.getStudentsInSession());
                //logic for add date,show date
                Date date = updateSession.get().getTime();
                Calendar cal = Calendar.getInstance(
                        TimeZone.getTimeZone("Asia/Bangkok"));
                cal.setTime(date);
                dialogSelectedYear[0]   = cal.get(Calendar.YEAR);
                dialogSelectedMonth[0]  = cal.get(Calendar.MONTH);
                dialogSelectedDay[0]    = cal.get(Calendar.DAY_OF_MONTH);
                dialogSelectedHour[0]   = cal.get(Calendar.HOUR_OF_DAY);
                dialogSelectedMinute[0] = cal.get(Calendar.MINUTE);
                String dateString = String.format(Locale.getDefault(), "%02d/%02d/%d", dialogSelectedDay[0]  , dialogSelectedMonth[0] + 1, dialogSelectedYear[0]);
                String timeString = String.format(Locale.getDefault(), "%02d:%02d", dialogSelectedHour[0], dialogSelectedMinute[0]);
                textViewSelectDateTime.setText(dateString + " " + timeString);

                session_LineraLay_dialog.removeAllViews();
                emptyStateTextView.setVisibility(View.GONE);
                for (String studentId : allStudent) {
                    CheckBox studentCheckbox = new CheckBox(TeacherAddAssessmentActivity.this);
                    UserController userController = new UserController();
                    userController.getUserIdDocument(studentId,(value, error)->{
                        if (value != null){
                            User student = value.toObject(User.class);
                            for (String studentToBeCheck : StudentAlreadyInSession) {
                                if(Objects.equals(studentId, studentToBeCheck)){
                                    studentCheckbox.setChecked(true);
                                }
                            }
                            studentCheckbox.setText(student.getEmail());
                            studentCheckbox.setTag(student.getUid());
                            studentCheckbox.setTextSize(16);
                            studentCheckbox.setTextColor(Color.BLACK);
                            studentCheckbox.setLayoutParams(checkboxParams);
                            session_LineraLay_dialog.addView(studentCheckbox);

                            //For random checkbox
                            studentCheckboxes.add(studentCheckbox);
                        }
                    });
                }
            });

            //submitting to update session
            submit_create_SessionBtn.setText("Cập nhật");
            submit_create_SessionBtn.setOnClickListener(v->{
                getCheckBoxAssessment(session_LineraLay_dialog,studentInSession,StudentToRemoveFromSession,true);
                String sessionName = SessionNameText.getText().toString();

                if (sessionName.isEmpty() || studentInSession.isEmpty()) {
                    Toast.makeText(TeacherAddAssessmentActivity.this, "Please complete the form (Name, Students, Date, and Time)!", Toast.LENGTH_SHORT).show();
                    studentInSession.clear();
                    return;
                }

                updateSession.get().setTitle(sessionName);
                updateSession.get().setStudentsInSession(studentInSession);

                //set time logic
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok"));
                cal.set(dialogSelectedYear[0], dialogSelectedMonth[0], dialogSelectedDay[0], dialogSelectedHour[0], dialogSelectedMinute[0], 0);
                Date selectedDateTime = cal.getTime();
                updateSession.get().setTime(selectedDateTime);

                
                //logic for setting student isSelected
                for (String studentId : StudentToRemoveFromSession) {
                    assessmentController.updateStudentSelection(Assessment_id, studentId, false, (task) -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Student selection updated successfully");
                        } else {
                            Log.w(TAG, "Error updating student selection");
                        }
                    });
                }
                for (String studentId : studentInSession) {
                    assessmentController.updateStudentSelection(Assessment_id, studentId, true, (task) -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Student selection updated successfully");
                        } else {
                            Log.w(TAG, "Error updating student selection");
                        }
                    });
                }

                assessmentController.updateSession(Assessment_id,sessionId, updateSession.get(),(task)-> {
                    if (task.isSuccessful()){
                        Toast.makeText(TeacherAddAssessmentActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });

        }

        dialog.show();
    }

    private void showDateTimePicker(
            final TextView dateTimeTextView,
            final int[] selectedYear,
            final int[] selectedMonth,
            final int[] selectedDay,
            final int[] selectedHour,
            final int[] selectedMinute) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if (selectedYear[0] != 0) {
            year = selectedYear[0];
            month = selectedMonth[0];
            day = selectedDay[0];
            hour = selectedHour[0];
            minute = selectedMinute[0];
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedYear[0] = year1;
                    selectedMonth[0] = monthOfYear;
                    selectedDay[0] = dayOfMonth;

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute1) -> {
                                selectedHour[0] = hourOfDay;
                                selectedMinute[0] = minute1;

                                String dateString = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay[0], selectedMonth[0] + 1, selectedYear[0]);
                                String timeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour[0], selectedMinute[0]);

                                dateTimeTextView.setText(dateString + " " + timeString);
                            }, selectedHour[0], selectedMinute[0], true);
                    timePickerDialog.show();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void getCheckBoxAssessment(LinearLayout student_list, List<String> studentInSession,List<String> studentNotSession,boolean isUpdate) {
        studentInSession.clear();
        for (int i = 0; i < student_list.getChildCount(); i++) {
            View child = student_list.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    String studentId = (String) checkBox.getTag();
                    if (studentId != null) {
                        studentInSession.add(studentId);
                    }
                } else if (isUpdate && !checkBox.isChecked()) {
                    String studentId = (String) checkBox.getTag();
                    if (studentId != null) {
                        studentNotSession.add(studentId);
                    }
                }
            }
        }
    }


    private void dialog_add_exam(boolean isUpdate, Exam updatedExam) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_teacher_create_exam, null);
        dialog.setContentView(dialogView);
        dialog.show();

        TextView ExamNameText = dialogView.findViewById(R.id.ExamNameText);
        EditText durationText = dialogView.findViewById(R.id.durationText);
        Button submit_create_ExamBtn = dialogView.findViewById(R.id.submit_create_ExamBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        List<String> session_arrayList = new ArrayList<>();
        LinearLayout session_list_exam_dialog = dialogView.findViewById(R.id.list_exam_dialog);
        TextView statusTextView = dialogView.findViewById(R.id.textView13);
        TextView emptyStateTextView = dialogView.findViewById(R.id.emptyStateTextView);

        //handel if when update there are session that already done
        AtomicBoolean isValid = new AtomicBoolean(true);

        if(isUpdate){
            ExamNameText.setText(updatedExam.getTitle());
            durationText.setText(String.valueOf(updatedExam.getDuration()));
            statusTextView.setText("Cập nhật đề");
        }

        AssessmentController assessmentController = new AssessmentController();
        assessmentController.getAllSessions(Assessment_id,(value,error)->{
            if(value!=null) {
                emptyStateTextView.setVisibility(View.GONE);
                session_list_exam_dialog.removeAllViews();
                for (DocumentSnapshot document : value.getDocuments()){
                    AssessmentSession assessmentSession = document.toObject(AssessmentSession.class);
                    CheckBox sessionCheckBox = new CheckBox(TeacherAddAssessmentActivity.this);

                    if (isUpdate) {
                        for ( String sessionId : updatedExam.getSessionId()) {
                            if(Objects.equals(assessmentSession.getId(),sessionId)){
                                sessionCheckBox.setChecked(true);

                                //disable checkbox session is no more valid and not allow to upload exam question
                                Date sessionDate = assessmentSession.getTime();
                                Date now = new Date();
                                if(now.after(sessionDate)) {
                                    long diffInMillis = now.getTime() - sessionDate.getTime();
                                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                                    sessionCheckBox.setEnabled(false);
                                    durationText.setEnabled(false);
                                    ExamNameText.setEnabled(false);
                                    isValid.set(false);
                                    submit_create_ExamBtn.setText("Chỉnh Ca Đề");
                                }
                            }
                        }
                    }

                    //disable checkbox if session is no more valid
                    Date sessionDate = assessmentSession.getTime();
                    Date now = new Date();
                    if(now.after(sessionDate)) {
                        long diffInMillis = now.getTime() - sessionDate.getTime();
                        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                        sessionCheckBox.setEnabled(false);
                    }


                    sessionCheckBox.setTag(assessmentSession.getId());
                    sessionCheckBox.setText("Ca thi: "+assessmentSession.getTitle());
                    sessionCheckBox.setTextSize(16);
                    sessionCheckBox.setTextColor(Color.BLACK);
                    session_list_exam_dialog.addView(sessionCheckBox);
                }
            }
        });

        submit_create_ExamBtn.setOnClickListener(v -> {
            String examName = ExamNameText.getText().toString();
            String duration = durationText.getText().toString();
            int durationInt;
            try{
                durationInt = Integer.parseInt(duration);
            }catch (Exception e){
                Toast.makeText(this, "Thời gian làm bài phải là số nguyên!", Toast.LENGTH_SHORT).show();
                return;
            }

            getCheckBoxAssessment(session_list_exam_dialog,session_arrayList,null,false );
            if (examName.isEmpty() || duration.isEmpty() || session_arrayList.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TeacherAddAssessmentActivity.this, TeacherAddExamManualActivity.class);
            if(isUpdate){
                intent.putExtra("EXAM_ID",updatedExam.getId());
                intent.putExtra("CREATED_BY", updatedExam.getCreatedBy());

                if (!isValid.get()) {
                    ExamController examController = new ExamController();
                    examController.updateSessionIds(updatedExam.getId(),session_arrayList,task ->{
                        if (task.isSuccessful()){
                            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    return;
                }
            }
            intent.putExtra("ASSESSMENT_ID", Assessment_id);
            intent.putExtra("EXAM_TITLE", examName);
            intent.putExtra("DURATION", durationInt);
            intent.putStringArrayListExtra("SESSION_LIST", (ArrayList<String>) session_arrayList);
            startActivity(intent);
            dialog.dismiss();
        });

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void fetchSessionExamToScrollView() {
        AssessmentController assessmentController = new AssessmentController();
        assessmentController.getAllSessions(Assessment_id,(value,error)->{
            if (value != null){
                list_session_ln.removeAllViews();
                emptyStateTextView1.setVisibility(View.GONE);
                for (DocumentSnapshot document : value.getDocuments()) {
                    AssessmentSession assessmentSession = document.toObject(AssessmentSession.class);
                    if (assessmentSession != null) {
                        TextView session_name = new TextView(TeacherAddAssessmentActivity.this);
                        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        questionParams.setMargins(16, 32, 16, 8);
                        session_name.setLayoutParams(questionParams);
                        session_name.setBackgroundResource(R.drawable.light_blue);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM, yyyy, HH:mm", new Locale("vi", "VN"));
                        String formattedDate = formatter.format(assessmentSession.getTime());
                        session_name.setText("Ca thi: "+ assessmentSession.getTitle()+" | Giờ Thi: " + formattedDate);
                        int paddingPx = dpToPx(16);
                        session_name.setPadding(paddingPx,dpToPx(5),paddingPx,dpToPx(5));
                        session_name.setTextSize(20);
                        session_name.setTypeface(null, Typeface.ITALIC);
                        session_name.setTextColor(Color.BLACK);

                        session_name.setOnClickListener(v -> {
                            Date sessionDate = assessmentSession.getTime();
                            Date now = new Date();
                            if(now.after(sessionDate)) {
                                long diffInMillis = now.getTime() - sessionDate.getTime();
                                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                                Log.d("SessionCheck", "Thời gian cách biệt: " + diffInMinutes + " phút");
                                if(diffInMinutes <= 30) {

                                    Toast.makeText(TeacherAddAssessmentActivity.this, "Ca thi đang trong giờ thi!", Toast.LENGTH_SHORT).show();
                                } else {

                                    Toast.makeText(TeacherAddAssessmentActivity.this, "Ca thi đã quá hạn không thể sửa!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                dialog_add_session(true,assessmentSession.getId());
                            }

                        });

                        session_name.setOnLongClickListener(v->{
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage("Xóa ca thi: "+assessmentSession.getTitle() )
                                        .setTitle("Xác nhận xóa");

                                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ExamController examController = new ExamController();
                                        examController.fetchExamsBySessionId(assessmentSession.getId(),(valueSEC, errorSEC)->{
                                            if (valueSEC.isEmpty()){
                                                assessmentController.updateArrayOfStudentSelection(Assessment_id,assessmentSession.getStudentsInSession(),false,task->{
                                                    if (task.isSuccessful()){
                                                        Log.d(TAG, "updateArrayOfStudentSelection: " + assessmentSession.getStudentsInSession());
                                                    }
                                                });
                                                assessmentController.deleteSession(Assessment_id,assessmentSession.getId(),task->{
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(TeacherAddAssessmentActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(TeacherAddAssessmentActivity.this, "Không thể xóa ca thi đang có đề!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });

                                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog dialog = builder.create();

                                dialog.show();
                                return true;
                        });

                        list_session_ln.addView(session_name);
                    }
                }

            }
        });

        ExamController examController = new ExamController();
        examController.fetchExamDocumentByAssessmentId(Assessment_id, (value, error) -> {
            if (value != null) {
                list_exam_ln.removeAllViews();
                emptyStateTextView2.setVisibility(View.GONE);
                for (DocumentSnapshot document : value.getDocuments()) {
                    Exam exam = document.toObject(Exam.class);

                    LinearLayout examContainer = new LinearLayout(TeacherAddAssessmentActivity.this);
                    examContainer.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    containerParams.setMargins(16, 32, 16, 8);
                    examContainer.setLayoutParams(containerParams);

                    TextView exam_name = new TextView(TeacherAddAssessmentActivity.this);
                    exam_name.setText("Đề thi: "+ exam.getTitle() + " | Thời gian: " + exam.getDuration() + " phút");
                    exam_name.setBackgroundResource(R.drawable.light_blue);
                    int paddingPx = dpToPx(16);
                    exam_name.setPadding(paddingPx,dpToPx(5),paddingPx,dpToPx(5));
                    exam_name.setTextSize(20);
                    exam_name.setTypeface(null, Typeface.ITALIC);
                    exam_name.setTextColor(Color.BLACK);
                    exam_name.setElevation(dpToPx(6));
                    exam_name.setTranslationZ(dpToPx(6));

                    examContainer.addView(exam_name);

                    // Create a container specifically for session views
                    LinearLayout sessionContainer = new LinearLayout(TeacherAddAssessmentActivity.this);
                    sessionContainer.setOrientation(LinearLayout.VERTICAL);
                    examContainer.addView(sessionContainer);

                    // Keep track of sessions and use a Set to prevent duplicates
                    Set<String> addedSessions = new HashSet<>();
                    final int totalSessions = exam.getSessionId().size();
                    final AtomicInteger loadedSessions = new AtomicInteger(0);
                    AtomicBoolean canExamBeUpdate = new AtomicBoolean(true);

                    for (String sessionId : exam.getSessionId()) {
                        AssessmentController assessmentController1 = new AssessmentController();
                        assessmentController1.getSessionsById(Assessment_id, sessionId, (value1, error1) -> {
                            if(value1 != null) {
                                AssessmentSession assessmentSession = value1.toObject(AssessmentSession.class);
                                assert assessmentSession != null;
                                String sessionName = assessmentSession.getTitle();

                                // Prevent duplicate sessions
                                if (!addedSessions.contains(sessionId)) {
                                    addedSessions.add(sessionId);

                                    TextView session_name = new TextView(TeacherAddAssessmentActivity.this);
                                    Log.d(TAG, "fetchExamDocumentByAssessmentId: " + value.size());
                                    session_name.setText("Ca thi: " + sessionName);
                                    session_name.setPadding(paddingPx, dpToPx(5), paddingPx, dpToPx(5));
                                    session_name.setTextSize(16);
                                    session_name.setTextColor(Color.DKGRAY);

                                    sessionContainer.addView(session_name);


                                }

                                // Check if this is the last session to load
                                if (loadedSessions.incrementAndGet() == totalSessions) {
                                    examContainer.setBackgroundResource(R.drawable.blue_box);
                                }

                                examContainer.setOnClickListener(v -> {
                                    if (canExamBeUpdate.get()) dialog_add_exam(true, exam);
                                });
                            }
                        });
                    }

                    //Delete exam
                    examContainer.setOnLongClickListener(v->{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Xóa đề thi: "+exam.getTitle() )
                                .setTitle("Xác nhận xóa");

                        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SubmissionController submissionController = new SubmissionController();
                                submissionController.getSubmissionByExamId(exam.getId(),(submission)->{
                                    if(submission == null){
                                        examController.deleteExam(exam.getId(),task->{
                                            if (task.isSuccessful()){
                                                Toast.makeText(TeacherAddAssessmentActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                                                fetchSessionExamToScrollView();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(TeacherAddAssessmentActivity.this, "Không thể xóa đề thi đã có bài nộp!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog dialog = builder.create();

                        dialog.show();
                        return true;
                    });

                    list_exam_ln.addView(examContainer);
                }
            }
        });
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}