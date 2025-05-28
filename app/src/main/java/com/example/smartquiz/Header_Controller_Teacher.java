package com.example.smartquiz;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.TeacherActiviy.TeacherAddExamManualActivity;
import com.example.smartquiz.TeacherActiviy.TeacherListClassActivity;
import com.example.smartquiz.TeacherActiviy.TeacherMainActivity;

public class Header_Controller_Teacher {
    private static boolean isMenuVisible = false;

    public static void setup(final Activity activity) {
        LinearLayout floatingMenu = activity.findViewById(R.id.floating_menu);
        ImageView menuButton = activity.findViewById(R.id.registerBtn);
        TextView homeButton = activity.findViewById(R.id.menu_home);
        TextView classes = activity.findViewById(R.id.classes);
        TextView uploadTest = activity.findViewById(R.id.menu_test);
        TextView createTest = activity.findViewById(R.id.menu_answer);
        menuButton.setOnClickListener(v -> {
            if (isMenuVisible) {
                floatingMenu.setVisibility(View.GONE);
            } else {
                floatingMenu.setVisibility(View.VISIBLE);
            }
            isMenuVisible = !isMenuVisible;
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TeacherMainActivity.class);
            activity.startActivity(intent);
        });

        classes.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TeacherListClassActivity.class);
            activity.startActivity(intent);
        });

        createTest.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TeacherAddExamManualActivity.class);
            activity.startActivity(intent);
        });

//        takeTestButton.setOnClickListener(v -> {
//            Intent intent = new Intent(activity, ListTestActivity.class);
//            activity.startActivity(intent);
//        });
//
//        answerButton.setOnClickListener(v->{
//            Intent intent = new Intent(activity, MainActivity.class);
//            activity.startActivity(intent);
//        });

    }
}
