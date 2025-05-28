package com.example.smartquiz;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;

public class Header_Controller {
    private static boolean isMenuVisible = false;
    private static UserController userController;

    public static void setup(final Activity activity) {
        LinearLayout floatingMenu = activity.findViewById(R.id.floating_menu);
        LinearLayout logoutScroll = activity.findViewById(R.id.logout_scroll);
        ImageView menuButton = activity.findViewById(R.id.registerBtn);
        ImageView userIcon = activity.findViewById(R.id.user_icon);
        TextView takeTestButton = activity.findViewById(R.id.menu_test);
        TextView homeButton = activity.findViewById(R.id.menu_home);
        TextView answerButton = activity.findViewById(R.id.menu_answer);
        TextView logout = activity.findViewById(R.id.logout);
        TextView userName = activity.findViewById(R.id.user_name);

        userController = new UserController();
        String userId = userController.getCurrentUserId();

        if (userId != null) {
            userController.getCurrentUserDocument((value, error) -> {
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    Log.d("DEBUG_TAG", "User ID: " + userId);
                    userName.setText(user.getName());
                }
            });
        }

        // Ẩn logout ban đầu
        logoutScroll.setVisibility(View.GONE);
        logoutScroll.setTranslationX(logoutScroll.getWidth());

        // Avatar click: toggle logout panel
        userIcon.setOnClickListener(v -> {
            if (logoutScroll.getVisibility() == View.VISIBLE) {
                logoutScroll.animate()
                        .translationX(logoutScroll.getWidth())
                        .setDuration(300)
                        .withEndAction(() -> logoutScroll.setVisibility(View.GONE));
            } else {
                logoutScroll.setVisibility(View.VISIBLE);
                logoutScroll.setTranslationX(logoutScroll.getWidth());
                logoutScroll.animate().translationX(0).setDuration(300);
            }
        });

        logout.setOnClickListener(v -> {
            userController.signOut();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            floatingMenu.setVisibility(isMenuVisible ? View.GONE : View.VISIBLE);
            isMenuVisible = !isMenuVisible;
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });

        takeTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ListTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });

        answerButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, testHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });
    }
}
