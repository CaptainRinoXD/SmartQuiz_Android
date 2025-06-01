package com.example.smartquiz.TeacherActiviy;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartquiz.R;
import com.example.smartquiz.Model.Question;
import com.example.smartquiz.ServiceModelAPI.ApiClient;
import com.example.smartquiz.ServiceModelAPI.QuizParserService;
import com.example.smartquiz.ServiceModelAPI.QuizParseResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherUploadDialog extends Dialog {

    private static final String TAG = "TeacherUploadDialog";
    private UploadDialogListener listener;
    private Uri selectedFileUri;
    private TextView tvSelectedFile;
    private Button btnSubmitExam;
    private Button btnSelectFile;

    public interface UploadDialogListener {
        void onUploadSuccess(List<Question> questions, String message);
        void onUploadError(String errorMessage);
        void onCancelClicked();
        void onFileSelectionRequested(); // Activity handles file selection
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

        initializeViews(dialogView);
        setupClickListeners();
    }

    private void initializeViews(View dialogView) {
        btnSubmitExam = dialogView.findViewById(R.id.btn_submit_exam);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        btnSelectFile = dialogView.findViewById(R.id.btn_select_file);
        tvSelectedFile = dialogView.findViewById(R.id.tv_selected_file);

        // Initially disable submit button until file is selected
        btnSubmitExam.setEnabled(false);
        btnSubmitExam.setAlpha(0.5f);
    }

    private void setupClickListeners() {
        btnSelectFile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileSelectionRequested();
            }
        });

        btnSubmitExam.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                uploadDocument();
            } else {
                Toast.makeText(getContext(), "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClicked();
            }
            dismiss();
        });
    }

    // Call this method from activity after file selection
    public void onFileSelected(Uri fileUri) {
        this.selectedFileUri = fileUri;
        if (selectedFileUri != null) {
            displaySelectedFile();
            enableSubmitButton();
        }
    }

    private void displaySelectedFile() {
        String fileName = getFileName(selectedFileUri);
        if (tvSelectedFile != null) {
            tvSelectedFile.setText("Selected: " + fileName);
            tvSelectedFile.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "File selected: " + fileName);
    }

    private void enableSubmitButton() {
        btnSubmitExam.setEnabled(true);
        btnSubmitExam.setAlpha(1.0f);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadDocument() {
        if (selectedFileUri == null) {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnSubmitExam.setEnabled(false);
        btnSubmitExam.setText("Uploading...");

        try {
            // Convert URI to File for upload
            File file = createTempFileFromUri(selectedFileUri);
            if (file == null) {
                showError("Failed to prepare file for upload");
                return;
            }

            // Create request body
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("document", file.getName(), requestFile);

            // Make API call
            QuizParserService service = ApiClient.getQuizParserService();
            Call<QuizParseResponse> call = service.parseQuizDocument(body);

            call.enqueue(new Callback<QuizParseResponse>() {
                @Override
                public void onResponse(Call<QuizParseResponse> call, Response<QuizParseResponse> response) {
                    // Reset button state
                    btnSubmitExam.setEnabled(true);
                    btnSubmitExam.setText("Upload & Parse");

                    if (response.isSuccessful() && response.body() != null) {
                        QuizParseResponse parseResponse = response.body();

                        if (parseResponse.isSuccess() && parseResponse.getQuestions() != null) {
                            Log.d(TAG, "Upload successful. Questions parsed: " + parseResponse.getQuestions().size());

                            // Clean up temp file
                            if (file.exists()) {
                                file.delete();
                            }

                            // Return success with questions
                            if (listener != null) {
                                listener.onUploadSuccess(parseResponse.getQuestions(), parseResponse.getMessage());
                            }
                            dismiss();

                        } else {
                            String errorMsg = parseResponse.getMessage() != null ?
                                    parseResponse.getMessage() : "Failed to parse questions from document";
                            showError(errorMsg);
                        }
                    } else {
                        showError("Server error: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<QuizParseResponse> call, Throwable t) {
                    Log.e(TAG, "Upload failed", t);

                    // Reset button state
                    btnSubmitExam.setEnabled(true);
                    btnSubmitExam.setText("Upload & Parse");

                    // Clean up temp file
                    if (file.exists()) {
                        file.delete();
                    }

                    String errorMessage = "Upload failed: " + t.getMessage();
                    showError(errorMessage);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing upload", e);
            showError("Error preparing file: " + e.getMessage());

            // Reset button state
            btnSubmitExam.setEnabled(true);
            btnSubmitExam.setText("Upload & Parse");
        }
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create temp file
            String fileName = getFileName(uri);
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }

            File tempFile = File.createTempFile("upload_doc", extension, getContext().getCacheDir());

            // Copy content to temp file
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error creating temp file", e);
            return null;
        }
    }

    private void showError(String message) {
        Log.e(TAG, "Error: " + message);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

        if (listener != null) {
            listener.onUploadError(message);
        }
    }

    @Override
    public void dismiss() {
        // Clean up any resources if needed
        super.dismiss();
    }
}