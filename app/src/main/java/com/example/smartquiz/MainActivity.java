package com.example.smartquiz;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {


   // private  Header_Controller headerController;
    private UserController userController;

    private TextView totalExamNumTextView;
    private TextView totalTimeNumTextView;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        lineChart = findViewById(R.id.lineChart);  // lấy view lineChart

        Header_Controller.setup(this);

        userController = new UserController();
        String userId = userController.getCurrentUserId();

        totalExamNumTextView = findViewById(R.id.total_exam_num);
        totalTimeNumTextView = findViewById(R.id.total_time_num);

        if (userId != null) {
            SubmissionController submissionController = new SubmissionController();
            submissionController.getSubmissionCountForStudent((value) -> {});
            //submissionController.getTotalDurationForStudent((value) -> {});

            userController.getCurrentUserDocument((value, error) -> {
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    int totalExams = user.getTotalExams();
                    long totalTime = user.getTotalTime();
                    totalExamNumTextView.setText(String.valueOf(totalExams));
                    totalTimeNumTextView.setText(String.valueOf(totalTime));

                    Map<String, Long> testData = new HashMap<>();
                    testData.put("2025-05-15", 3L);
                    testData.put("2025-05-16", 7L);
                    testData.put("2025-05-17", 5L);
                    testData.put("2025-05-18", 8L);
                    testData.put("2025-06-19", 6L);
                    testData.put("2025-07-19", 6L);
                    testData.put("2025-08-19", 6L);
                    testData.put("2025-09-19", 10L);
                    testData.put("2025-10-19", 6L);
                    testData.put("2025-11-19", 6L);


                    showLineChart(testData);

                }
            });
        }
    }

    private void showLineChart(Map<String, Long> exerciseHistory) {
        // Sắp xếp map theo ngày tăng dần
        Map<String, Long> sortedMap = new TreeMap<>(exerciseHistory);

        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int index = 0;
        for (String dateStr : sortedMap.keySet()) {
            dates.add(dateStr);
            long count = sortedMap.get(dateStr);
            entries.add(new Entry(index, count));
            index++;
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Số bài làm mỗi ngày");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(7f);
        lineDataSet.setCircleColor(Color.RED);



        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Định dạng trục X hiển thị ngày tháng (chỉ MM-dd cho gọn)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextSize(12f);
        lineChart.getAxisLeft().setTextSize(12f); // ← tăng size chữ trục Y bên trái
        lineChart.getAxisRight().setEnabled(false);
        lineDataSet.setValueTextSize(12f);
        lineChart.getLegend().setTextSize(10f);
        lineChart.getLegend().setEnabled(false);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(0f); // không xoay
        lineChart.setExtraBottomOffset(24f); // tăng khoảng cách dưới

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < dates.size()) {
                    String[] parts = dates.get(i).split("-");
                    if (parts.length == 3) {
                        return parts[2] + "-" + parts[1]; // dd-MM
                    }
                }
                return "";
            }
        });

        // Cấu hình chú thích (legend) không nằm đè lên trục X
        lineChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        lineChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        lineChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        lineChart.getLegend().setDrawInside(false); // rất quan trọng: vẽ ngoài vùng biểu đồ

        // Thêm khoảng cách bên dưới để không bị đè lên nhãn trục X
        lineChart.setExtraBottomOffset(10f);

        // Tăng kích thước tối thiểu trục X nếu số lượng ngày quá nhiều
        int minDaysVisible = 5;
        int totalDays = dates.size();

        lineChart.setVisibleXRangeMaximum(minDaysVisible); // tối thiểu bao nhiêu ngày hiển thị cùng lúc

        // Cho phép cuộn ngang nếu dữ liệu nhiều
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false); // không cho zoom để tránh rối

        // Di chuyển về điểm cuối cùng (mới nhất)
        lineChart.moveViewToX(totalDays);



        lineChart.getDescription().setEnabled(false);

        lineChart.invalidate(); // cập nhật biểu đồ
    }
}
