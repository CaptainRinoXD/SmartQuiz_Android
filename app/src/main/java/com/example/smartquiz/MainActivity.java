package com.example.smartquiz;

import android.graphics.Color;
import android.widget.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.widget.TextView;

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
    private GridLayout heatmapGrid;

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
        heatmapGrid = findViewById(R.id.heatmapGrid2);


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

                    Map<String, Long> exerciseHistory = (Map<String, Long>) value.get("exerciseHistory");
                    if (exerciseHistory == null) exerciseHistory = new HashMap<>();

                    showHeatmap(exerciseHistory);

                }
            });
        }
    }
    private void showHeatmap(Map<String, Long> exerciseHistory) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        heatmapGrid.removeAllViews(); // xóa cũ nếu có

        for (int i = 0; i < 42; i++) {
            TextView dayView = new TextView(this);
            dayView.setWidth(100);
            dayView.setHeight(100);
            dayView.setBackgroundColor(Color.LTGRAY); // màu mặc định
            dayView.setText(""); // có thể đặt số nếu muốn

            String dateStr = sdf.format(calendar.getTime());

            if (exerciseHistory.containsKey(dateStr)) {
                long count = exerciseHistory.get(dateStr);
                int color = getColorForCount((int) count);
                dayView.setBackgroundColor(color);
            }

            heatmapGrid.addView(dayView);
            calendar.add(Calendar.DATE, -1); // lùi 1 ngày
        }
    }

    private int getColorForCount(int count) {
        if (count == 0) return Color.LTGRAY;
        else if (count == 1) return Color.parseColor("#C8E6C9"); // light green
        else if (count <= 3) return Color.parseColor("#81C784"); // medium green
        else return Color.parseColor("#388E3C"); // dark green
    }


}
