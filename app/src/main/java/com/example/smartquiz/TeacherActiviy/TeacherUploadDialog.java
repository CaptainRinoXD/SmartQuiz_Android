package com.example.smartquiz.TeacherActiviy;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.smartquiz.R;

public class TeacherUploadDialog extends Dialog {

    private UploadDialogListener listener;

    public interface UploadDialogListener {
        void onUploadConfirmed();
        void onCancelClicked();
    }

    public TeacherUploadDialog(Context context, UploadDialogListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_teacher_upload, null);
        setContentView(dialogView);

        Button btnSubmitExam = dialogView.findViewById(R.id.btn_submit_exam);
        Button cancel_button = dialogView.findViewById(R.id.cancel_button);

        btnSubmitExam.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUploadConfirmed();
            }
            dismiss(); // Close the dialog after action
        });

        cancel_button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClicked();
            }
            dismiss(); // Close the dialog after action
        });
    }
}