package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.Submission;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SubmissionController extends BaseController {
    public void submitExam(Submission submission, OnCompleteListener<Void> listener) {
        submission.setStudentId(getCurrentUserId());
        submission.setSubmittedAt(new java.util.Date());

        String submissionId = db.collection("submissions").document().getId();
        db.collection("submissions").document(submissionId).set(submission)
                .addOnCompleteListener(listener);
    }

    public void getStudentSubmission(String examId, OnSuccessListener<Submission> listener) {
        db.collection("submissions")
                .whereEqualTo("examId", examId)
                .whereEqualTo("studentId", getCurrentUserId())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        Submission s = snapshots.getDocuments().get(0).toObject(Submission.class);
                        listener.onSuccess(s);
                    } else {
                        listener.onSuccess(null);
                    }
                });
    }
}
