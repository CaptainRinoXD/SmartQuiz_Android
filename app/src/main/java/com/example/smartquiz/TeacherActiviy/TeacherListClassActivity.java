package com.example.smartquiz.TeacherActiviy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Header_Controller_Teacher;
import com.example.smartquiz.R;

public class TeacherListClassActivity extends AppCompatActivity {
    ImageButton createClassButton;
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

        createClassButton = findViewById(R.id.add_btnClass);
        createClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherListClassActivity.this, TeacherAddClassActivity.class);
            startActivity(intent);
        });

    }
}