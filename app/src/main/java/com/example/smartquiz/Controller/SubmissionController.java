package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.Submission;
import com.example.smartquiz.Model.SubmissionAnswer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SubmissionController extends BaseController {
    public void submitExam(Submission submission, int duration, OnCompleteListener<Void> listener) {
        submission.setStudentId(getCurrentUserId());
        submission.setSubmittedAt(new Date());
        submission.setDuration(duration);

        // Lấy tất cả câu hỏi của bài thi từ Firestore
        db.collection("exams")
                .document(submission.getExamId())
                .collection("questions")
                .get()
                .addOnSuccessListener(questionSnapshots -> {
                    int correctCount = 0;

                    // Duyệt từng câu trả lời học sinh đã chọn
                    for (SubmissionAnswer answer : submission.getAnswers()) {
                        // Tìm câu hỏi tương ứng trong danh sách
                        for (DocumentSnapshot qSnap : questionSnapshots) {
                            if (qSnap.getId().equals(answer.getQuestionId())) {
                                // Lấy danh sách đáp án từ Firestore
                                List<Map<String, Object>> answersList =
                                        (List<Map<String, Object>>) qSnap.get("answers");

                                int correctIndex = -1;
                                for (int i = 0; i < answersList.size(); i++) {
                                    Map<String, Object> ans = answersList.get(i);
                                    if ((boolean) ans.get("isCorrect")) {
                                        correctIndex = i;
                                        break;
                                    }
                                }

                                // Gán kết quả vào answer
                                answer.setCorrectAnswerIndex(correctIndex);
                                boolean isCorrect = (answer.getSelectedAnswerIndex() == correctIndex);
                                answer.setIsCorrect(isCorrect);
                                if (isCorrect) correctCount++;
                                break;
                            }
                        }
                    }

                    // Cập nhật điểm
                    submission.setScore(correctCount);

                    // Lưu submission vào Firestore
                    String submissionId = db.collection("submissions").document().getId();
                    db.collection("submissions").document(submissionId)
                            .set(submission)
                            .addOnCompleteListener(listener);
                });
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

    public void getAllSubmissionsForStudent(OnSuccessListener<List<Submission>> listener) {
        String studentId = getCurrentUserId();

        db.collection("submissions")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Submission> submissions = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Submission submission = document.toObject(Submission.class);
                        submissions.add(submission);
                    }
                    listener.onSuccess(submissions);
                });
    }

    public void getTotalDurationForStudent(OnSuccessListener<Integer> listener) {
        String studentId = getCurrentUserId();

        db.collection("submissions")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalDuration = 0;
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Submission submission = document.toObject(Submission.class);
                        totalDuration += submission.getDuration();
                    }
                    listener.onSuccess(totalDuration);
                });
    }

    public void getSubmissionCountForStudent(OnSuccessListener<Integer> listener) {
        String studentId = getCurrentUserId();

        db.collection("submissions")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int submissionCount = querySnapshot.size();
                    listener.onSuccess(submissionCount);
                });
    }

}
