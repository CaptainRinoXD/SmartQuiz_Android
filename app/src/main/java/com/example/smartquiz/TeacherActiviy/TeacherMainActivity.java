package com.example.smartquiz.TeacherActiviy;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.ClassesController;
import com.example.smartquiz.Controller.ExamController;
import com.example.smartquiz.Header_Controller;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.R;

public class TeacherMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.teacher_activity_main);
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

        TextView total_exam_num = findViewById(R.id.total_exam_num);
        TextView total_class = findViewById(R.id.total_class);

        ExamController examController = new ExamController();
        examController.fetchAllExamsByUserId((value,error)->{
            if (value == null) {return;}
            total_exam_num.setText(String.valueOf(value.size()));
        });

        ClassesController classesController = new ClassesController();
        classesController.getClassByCreatedBy((task)->{
            if (task.isSuccessful()) {
                total_class.setText(String.valueOf(task.getResult().size()));

            }
        });
    }
}