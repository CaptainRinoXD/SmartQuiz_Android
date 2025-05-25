package com.example.smartquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartquiz.Controller.UserController;
import com.example.smartquiz.Model.User;
import com.example.smartquiz.Util.LoadingDialogFragment;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText emailEditText;
    private EditText registerPasswordEditText;
    private EditText confirmPasswordEditText;
    private EditText registerUsernameEditText;
    private ImageButton registerBtn;
    private ImageButton cancelBtn;
    private UserController userController;
    private RadioGroup roleRadioGroup;
    private RadioButton SelectedroleRadioButton;

    private LoadingDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // assign UI component
        emailEditText = findViewById(R.id.editTextEmail);
        registerPasswordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerUsernameEditText = findViewById(R.id.editTextName);
        registerBtn = findViewById(R.id.registerBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        roleRadioGroup = findViewById(R.id.radioGroupRole);

        userController = new UserController();

        registerBtn.setOnClickListener(view -> {
            // convert to String
            String email = emailEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String username = registerUsernameEditText.getText().toString();

            int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
            SelectedroleRadioButton = findViewById(selectedRoleId);
            String role = SelectedroleRadioButton.getText().toString();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            } else if (!password.equals(confirmPassword))  {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            registerUser(email, password, username, role);
        });

        cancelBtn.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser(String email, String password, String username, String role) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(username);
        newUser.setRole(role);
        showLoading(true);
        userController.signUpUserDocument(email, password, newUser, task-> {
            showLoading(false);
            if(task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.w(TAG, "signUpUserDocument:failure", task.getException());
                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        } );
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null || !loadingDialog.isVisible()) {
                loadingDialog = LoadingDialogFragment.newInstance();
                // Ensure LoadingDialogFragment.TAG is defined in LoadingDialogFragment
                loadingDialog.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
            }
            setUiEnabled(false);
        } else {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismissAllowingStateLoss();
            }
            loadingDialog = null; // Clear reference
            setUiEnabled(true);
        }
    }

    private void setUiEnabled(boolean enabled) {
        emailEditText.setEnabled(enabled);
        registerPasswordEditText.setEnabled(enabled);
        confirmPasswordEditText.setEnabled(enabled);
        registerUsernameEditText.setEnabled(enabled);
        registerBtn.setEnabled(enabled);
        cancelBtn.setEnabled(enabled);
        for (int i = 0; i < roleRadioGroup.getChildCount(); i++) {
            roleRadioGroup.getChildAt(i).setEnabled(enabled);
        }
    }
}