package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Util.LoadingDialogFragment;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {
    private LoadingDialogFragment loadingDialog;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //defined UI component
        ImageButton loginBtn = findViewById(R.id.loginBtn);
        ImageButton registerBtn = findViewById(R.id.registerBtn);
        EditText emailText = findViewById(R.id.email_text);
        EditText passwordText = findViewById(R.id.password_text);

        UserController userController = new UserController();

        loginBtn.setOnClickListener(view -> {
             String email = emailText.getText().toString();
             String password = passwordText.getText().toString();

             if (email.isEmpty() || password.isEmpty()) {
                 Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                 return;
             }
             showLoading(true);

             userController.signInWithEmailPassword(email, password, task -> {
                 showLoading(false);
                 if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null) {
                        intent.putExtra("userId", user.getUid());
                        intent.putExtra("userEmail", user.getEmail());
                        intent.putExtra("userName", user.getDisplayName());
                    }
                    startActivity(intent);
                    finish();
                 } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                 }
             });


        });

        
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null || !loadingDialog.isVisible()) {
                loadingDialog = LoadingDialogFragment.newInstance();
                // Ensure LoadingDialogFragment.TAG is defined in LoadingDialogFragment
                loadingDialog.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
            }

        } else {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismissAllowingStateLoss();
            }
            loadingDialog = null; // Clear reference

        }
    }
}