package com.example.smartquiz;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.TeacherActiviy.TeacherAddExamManualActivity;
import com.example.smartquiz.TeacherActiviy.TeacherListClassActivity;
import com.example.smartquiz.TeacherActiviy.TeacherMainActivity;

public class Header_Controller_Teacher {
    private static boolean isMenuVisible = false;
    private static UserController userController;

    public static void setup(final Activity activity) {
        userController = new UserController();

        // Inflate layout menu
        LayoutInflater inflater = LayoutInflater.from(activity);
        View menuView = inflater.inflate(R.layout.dialog_menu_teacher, null);

        // Add to root view
        ViewGroup root = activity.findViewById(android.R.id.content);
        root.addView(menuView);

        // Get references from the inflated menu
        LinearLayout floatingMenu = activity.findViewById(R.id.floating_menu);
        ImageView menuButton = activity.findViewById(R.id.registerBtn);
        TextView homeButton = activity.findViewById(R.id.menu_home);
        TextView classes = activity.findViewById(R.id.classes);
        TextView uploadTest = activity.findViewById(R.id.menu_test);
        TextView createTest = activity.findViewById(R.id.menu_answer);
        TextView logoutButton = menuView.findViewById(R.id.menu_logout_teacher);
        TextView userName = activity.findViewById(R.id.user_name);

        // Initially hide the menu if needed
        floatingMenu.setVisibility(View.GONE);

        userController = new UserController();
        String userId = userController.getCurrentUserId();

        if (userId != null) {
            // SubmissionController submissionController = new SubmissionController();

            userController.getCurrentUserDocument((value, error) -> {
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    Log.d("DEBUG_TAG", "User ID: " + userId);

                    userName.setText(user.getName()); // ✅ dòng đúng

                }
            });

        }
        // Show/hide logic — optional
        // You may connect a separate button (e.g. menu icon) to toggle visibility

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
            floatingMenu.setVisibility(View.GONE);
            isMenuVisible = false;
        });

        classes.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TeacherListClassActivity.class);
            activity.startActivity(intent);
            floatingMenu.setVisibility(View.GONE);
            isMenuVisible = false;
        });

        createTest.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TeacherAddExamManualActivity.class);
            activity.startActivity(intent);
            floatingMenu.setVisibility(View.GONE);
            isMenuVisible = false;
        });

        logoutButton.setOnClickListener(v -> {
            userController.signOut();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);

        });
    }
}

