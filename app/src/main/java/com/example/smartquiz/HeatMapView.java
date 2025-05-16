package com.example.smartquiz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HeatMapView extends View {
    private int[] dailyCounts = new int[7]; // Số bài mỗi ngày (Mon → Sun)
    private Paint paint = new Paint();

    public HeatMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDailyCounts(int[] counts) {
        if (counts.length == 7) {
            this.dailyCounts = counts;
            invalidate(); // vẽ lại khi có dữ liệu mới
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cellWidth = getWidth() / 7;
        int cellHeight = getHeight();

        int max = 0;
        for (int val : dailyCounts) {
            if (val > max) max = val;
        }
        if (max == 0) max = 1; // tránh chia 0

        for (int i = 0; i < 7; i++) {
            float ratio = (float) dailyCounts[i] / max;
            int red = (int) (255 * ratio);
            int green = 255 - red;
            paint.setColor(Color.rgb(red, green, 0));
            canvas.drawRect(i * cellWidth, 0, (i + 1) * cellWidth, cellHeight, paint);
        }
    }
}
