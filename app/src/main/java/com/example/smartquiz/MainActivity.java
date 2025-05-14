package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private LinearLayout floatingMenu;
    private ImageView menuButton;
    private TextView takeTestButton;
    private boolean isMenuVisible = false;
    private UserController userController;
    private TextView totalExamNumTextView;
    private TextView totalTimeNumTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Áp insets cho layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tìm view
        floatingMenu = findViewById(R.id.floating_menu);
        menuButton = findViewById(R.id.registerBtn);
        takeTestButton = findViewById(R.id.menu_test);

        // Gắn sự kiện toggle menu
        menuButton.setOnClickListener(v -> {
            if (isMenuVisible) {
                floatingMenu.setVisibility(View.GONE);

            } else {
                floatingMenu.setVisibility(View.VISIBLE);
            }
            isMenuVisible = !isMenuVisible;

        });

        takeTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListTestActivity.class);
            startActivity(intent);
        });


        userController = new UserController();
        String userId = userController.getCurrentUserId();

        totalExamNumTextView = findViewById(R.id.total_exam_num);
        totalTimeNumTextView = findViewById(R.id.total_time_num);



        if (userId != null) {
            userController.getCurrentUserDocument((value, error) -> {
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    int totalExams = user.getTotalExams();
                    long totalTime = user.getTotalTime();
                    totalExamNumTextView.setText(String.valueOf(totalExams));
                    totalTimeNumTextView.setText(String.valueOf(totalTime));
                }
            });
        }
    }
}
