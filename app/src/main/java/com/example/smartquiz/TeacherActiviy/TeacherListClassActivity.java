package com.example.smartquiz.TeacherActiviy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.ClassesController;
import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.smartquiz.Model.Class;

public class TeacherListClassActivity extends AppCompatActivity {
    ImageButton createClassButton;
    LinearLayout scroll_view_class_list;
    TextView emptyStateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.teacher_list_class);
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
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        createClassButton = findViewById(R.id.add_btnStudent);
        createClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherListClassActivity.this, TeacherAddClassActivity.class);
            startActivity(intent);
        });

        ClassesController classesController = new ClassesController();
        scroll_view_class_list = findViewById(R.id.scroll_view_class_list);
        classesController.getClassByCreatedBy(task->{
            if (task.isSuccessful()) {
                emptyStateTextView.setVisibility(View.GONE);
                for (DocumentSnapshot document : task.getResult()) {
                   Class aClass = document.toObject(Class.class);
                   if(aClass ==null) {return;}
                   addClassToLN(aClass);
                }
            }
        });
    }

    private void addClassToLN(Class aClass) {
        TextView examTextView = new TextView(this);
        examTextView.setText("Lớp: " + aClass.getName());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginVerticalPx = dpToPx(4);
        int paddingPx = dpToPx(16);

        params.setMargins(paddingPx, marginVerticalPx, paddingPx, marginVerticalPx);
        examTextView.setLayoutParams(params);

        examTextView.setBackgroundResource(R.drawable.light_blue);


        examTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        scroll_view_class_list.addView(examTextView);
        examTextView.setOnClickListener(v -> {
//            Intent intent = new Intent(TeacherListClassActivity.this, TeacherListStudentActivity.class);
//            intent.putExtra("CLASS_ID", aClass.getId());
//            startActivity(intent);
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}