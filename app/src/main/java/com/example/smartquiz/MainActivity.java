package com.example.smartquiz;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.Submission;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

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
        if (actionBar != null) actionBar.hide();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lineChart = findViewById(R.id.lineChart);
        Header_Controller.setup(this);

        userController = new UserController();
        totalExamNumTextView = findViewById(R.id.total_exam_num);
        totalTimeNumTextView = findViewById(R.id.total_time_num);

        String userId = userController.getCurrentUserId();
        if (userId != null) {
            SubmissionController submissionController = new SubmissionController();

            submissionController.getSubmissionCountForStudent(value -> {
                if (value != null) {
                    totalExamNumTextView.setText(String.valueOf(value));
                }
            });

            submissionController.getTotalDurationForStudent(value -> {
                if (value != null) {
                    totalTimeNumTextView.setText(String.valueOf(value));
                }
            });

            submissionController.getAllSubmissionsFromStudent(submissions -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Map<String, Long> dailyData = new TreeMap<>();

                for (Submission s : submissions) {
                    String dateKey = sdf.format(s.getSubmittedAt());
                    dailyData.put(dateKey, dailyData.getOrDefault(dateKey, 0L) + 1);
                }

                showLineChart(dailyData);
            });
        }
    }

    private void showLineChart(Map<String, Long> exerciseHistory) {
        Map<String, Long> sortedMap = new TreeMap<>(exerciseHistory);
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
            dates.add(entry.getKey());
            entries.add(new Entry(index, entry.getValue()));
            index++;
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Số bài làm mỗi ngày");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(7f);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setValueTextSize(12f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(0f);
        lineChart.setExtraBottomOffset(24f);

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

        lineChart.getAxisLeft().setTextSize(12f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);

        // ➤ Chỉ hiển thị 7 ngày gần nhất và cho phép cuộn
        lineChart.setVisibleXRangeMaximum(7);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.moveViewToX(entries.size() - 1); // cuộn đến điểm cuối

        lineChart.invalidate();
    }

}
