package com.example.smartquiz.Util;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smartquiz.R;

public class LoadingDialogFragment extends DialogFragment {

    public static final String TAG = "LoadingDialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.progress_dialog_layout, container, false);

        // Optional: Make the dialog window transparent if your FrameLayout doesn't have a background
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE); // Remove title bar
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Make it non-cancelable
            dialog.setCancelable(false);
            // Optional: Set dialog dimensions (e.g., full screen)
            // int width = ViewGroup.LayoutParams.MATCH_PARENT;
            // int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // dialog.getWindow().setLayout(width, height);
        }
    }

    // Static method to create a new instance (optional, but good practice)
    public static LoadingDialogFragment newInstance() {
        return new LoadingDialogFragment();
    }
}