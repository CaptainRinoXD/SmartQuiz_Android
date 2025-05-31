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

        createClassButton = findViewById(R.id.add_btn_student_session);
        createClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherListClassActivity.this, TeacherAddClassActivity.class);
            startActivity(intent);
        });


        scroll_view_class_list = findViewById(R.id.scroll_view_assessment_list);
        fetchDisplayClassEventLis();
    }
    private void fetchDisplayClass() {
        ClassesController classesController = new ClassesController();
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

    private void fetchDisplayClassEventLis() {
        ClassesController classesController = new ClassesController();
        classesController.getClassByCreatedByEventLis((value, error)->{
            if (value != null) {
                scroll_view_class_list.removeAllViews();
                emptyStateTextView.setVisibility(View.GONE);
                for (DocumentSnapshot document : value.getDocuments()) {
                    Class aClass = document.toObject(Class.class);
                    if(aClass ==null) {return;}
                    addClassToLN(aClass);
                }
            }
        });
    }

    private void addClassToLN(Class aClass) {
        TextView classTextView = new TextView(this);
        classTextView.setText("Lớp: " + aClass.getName());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginVerticalPx = dpToPx(4);
        int paddingPx = dpToPx(16);

        params.setMargins(paddingPx, marginVerticalPx, paddingPx, marginVerticalPx);
        classTextView.setLayoutParams(params);

        classTextView.setBackgroundResource(R.drawable.light_blue);


        classTextView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        scroll_view_class_list.addView(classTextView);
        classTextView.setOnClickListener(v -> {
           Intent intent = new Intent(TeacherListClassActivity.this, TeacherListAssessmentActivity.class);
           intent.putExtra("CLASS_ID", aClass.getId());
           startActivity(intent);
        });
        classTextView.setOnLongClickListener(v->{
            Intent intent = new Intent(TeacherListClassActivity.this, TeacherAddClassActivity.class);
            intent.putExtra("isUpdate",true);
            intent.putExtra("CLASS_ID", aClass.getId());
            startActivity(intent);
            return true;
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}