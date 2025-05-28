package com.example.smartquiz.TeacherActiviy;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

import com.example.smartquiz.Controller.ClassesController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.Model.Class;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherAddClassActivity extends AppCompatActivity {
    Button submitClassBtn;
    Button cancelClassBtn;
    ImageButton add_btnStudent;
    EditText classNameText;
    List<User> studentArayList;
    List<String> studentArrayListString;
    LinearLayout studentToAddList;
    TextView emptyStateTextView;
    private static final String TAG = "TeacherAddClassActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.teacher_add_class);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Header_Controller_Teacher.setup(this);
        submitClassBtn = findViewById(R.id.submit_create_ExamBtn);
        cancelClassBtn = findViewById(R.id.cancelClassBtn);
        add_btnStudent = findViewById(R.id.add_btnStudent);
        studentToAddList = findViewById(R.id.studentToAddList);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        classNameText = findViewById(R.id.classNameText);

        studentArayList = new ArrayList<>();
        studentArrayListString = new ArrayList<>();

        add_btnStudent.setOnClickListener(v -> {
            showAddStudentDialog();
        });

        submitClassBtn.setOnClickListener(v->{
            summitCreateClass();
        });
       

    }

    private void summitCreateClass() {
        String className = classNameText.getText().toString();
        if (className.isEmpty() || studentArrayListString.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên lớp.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ClassesController newClassroom = new ClassesController();
            Class newClass = new Class();
            newClass.setName(className);
            newClass.setStudentsId(studentArrayListString);
            newClassroom.createClass(newClass, task ->{
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Tạo lớp thành công.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tạo lớp thất bại.", Toast.LENGTH_SHORT).show();
                }
            });
            finish();
        }
    }

    private void refreshStudentList() {
        studentToAddList.removeAllViews();
        studentArrayListString.clear();
        if (studentArayList != null) {
            emptyStateTextView.setVisibility(View.GONE);
            for (User student : studentArayList) {
                if(student.getName()==null){return;}
                String name = student.getName();
                TextView studentTextView = new TextView(this);
                LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                questionParams.setMargins(16, 32, 16, 8);
                studentTextView.setLayoutParams(questionParams);
                studentTextView.setText("Học sinh: "+name);
                studentTextView.setTextSize(20);
                studentTextView.setTypeface(null, Typeface.ITALIC);
                studentTextView.setTextColor(Color.BLACK);

                if(student.getUid() != null){
                    studentArrayListString.add(student.getUid());
                }
                studentToAddList.addView(studentTextView);
            }
        }
    }

    private void showAddStudentDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true); // Allow dismissing by tapping outside or back button

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        dialog.setContentView(dialogView);

        LinearLayout dialogStudentListLayout = dialogView.findViewById(R.id.studentList);
        EditText editTextStudentEmail = dialogView.findViewById(R.id.editTextStudentEmail);
        Button buttonAddStudent = dialogView.findViewById(R.id.buttonAddStudentDialog);


        editTextStudentEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailPrefix = s.toString().trim();
                dialogStudentListLayout.removeAllViews();
                if (!emailPrefix.isEmpty()) {
                    UserController userController = new UserController();
                    userController.getUsersByEmailPrefixAndRole(emailPrefix, (task) -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            final RadioGroup searchResultsRadioGroup = new RadioGroup(TeacherAddClassActivity.this);
                            searchResultsRadioGroup.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams rgParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            rgParams.setMargins(16, 8, 16, 8);
                            searchResultsRadioGroup.setLayoutParams(rgParams);

                            if (querySnapshot != null) {
                                dialogStudentListLayout.removeAllViews();
                                Log.d(TAG, "Found " + querySnapshot.size() + " matching students.");
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    //Log.d(TAG, "Student: " + document.getData());
                                    User currUser = document.toObject(User.class);
                                    assert currUser != null;
                                    String userEmail = currUser.getEmail();

                                    RadioButton studentRadioButton = new RadioButton(TeacherAddClassActivity.this);
                                    studentRadioButton.setText(userEmail);
                                    studentRadioButton.setTag(currUser);
                                    studentRadioButton.setTextSize(16);
                                    studentRadioButton.setTextColor(Color.BLACK);

                                    LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT);
                                    rbParams.setMargins(8, 4, 8, 4); 
                                    studentRadioButton.setLayoutParams(rbParams);

                                    // Add the NEW RadioButton to the RadioGroup
                                    searchResultsRadioGroup.addView(studentRadioButton);
                                }
                                dialogStudentListLayout.addView(searchResultsRadioGroup);

                                if(editTextStudentEmail.getText().toString().isEmpty()) {
                                    Log.d(TAG, "Clear student list");
                                    dialogStudentListLayout.removeAllViews();
                                }
                            } else {
                                Log.d(TAG, "No matching students found.");
                            }
                        } else {
                            Log.e(TAG, "Error getting students: ", task.getException());
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        buttonAddStudent.setOnClickListener(v -> {
            //String studentEmail = editTextStudentEmail.getText().toString().trim();
            RadioGroup searchResultsRadioGroup = null;
            if (dialogStudentListLayout.getChildCount() > 0) {
                View firstChild = dialogStudentListLayout.getChildAt(0);
                if (firstChild instanceof RadioGroup) {
                    searchResultsRadioGroup = (RadioGroup) firstChild;
                }
            }
            if (searchResultsRadioGroup == null) {
                return;
            }
            int selectedRadioButtonId = searchResultsRadioGroup.getCheckedRadioButtonId();
            Log.d(TAG, "selectedRadioButtonId: " + selectedRadioButtonId);
            if (selectedRadioButtonId != -1) {
                RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);
                if(selectedRadioButton == null) {Log.d(TAG, "selectedRadioButton is null");return;}
                User selectedUser = (User) selectedRadioButton.getTag();
                String selectedUserId = selectedUser.getUid();
                String selectedUserEmail = selectedUser.getEmail();

                boolean alreadyExists = false;
                if (studentArayList != null) {
                    for (User u : studentArayList) {
                        if (u != null && u.getUid() != null && u.getUid().equals(selectedUserId)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                }

                if(alreadyExists) {
                    Toast.makeText(this, "Học sinh này đã được thêm rồi.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Học sinh " + selectedUserEmail + " đã tồn tại trong danh sách.");
                } else {
                    studentArayList.add(selectedUser);
                    refreshStudentList();
                    Toast.makeText(this, "Thêm học sinh với Email: " + selectedRadioButton.getText(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "studentArayList:" + studentArayList);
                    }

            } else {
                Toast.makeText(this, "Vui lòng chọn học sinh.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}