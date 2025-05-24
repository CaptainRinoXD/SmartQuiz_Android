package com.example.smartquiz;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartquiz.Controller.SubmissionController;
import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Header_Controller {
    private static boolean isMenuVisible = false;
    private static UserController userController;
    public static void setup(final Activity activity) {
        LinearLayout floatingMenu = activity.findViewById(R.id.floating_menu);
        ImageView menuButton = activity.findViewById(R.id.registerBtn);
        TextView takeTestButton = activity.findViewById(R.id.menu_test);
        TextView homeButton= activity.findViewById(R.id.menu_home);
        TextView answerButton = activity.findViewById(R.id.menu_answer);
        TextView userName = activity.findViewById(R.id.user_name);

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


        menuButton.setOnClickListener(v -> {
            if (isMenuVisible) {
                floatingMenu.setVisibility(View.GONE);
            } else {
                floatingMenu.setVisibility(View.VISIBLE);
            }
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

        answerButton.setOnClickListener(v->{
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });



    }
}
