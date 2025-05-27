package com.example.smartquiz.Controller;

import android.util.Log;

import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Question;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;

public class ExamController extends BaseController {
    public void createExam(Exam exam, List<Question> questions, OnCompleteListener<Void> listener) {
        String examId = db.collection("exams").document().getId();
        exam.setCreatedAt(new Date());
        exam.setCreatedBy(getCurrentUserId());

        DocumentReference examRef = db.collection("exams").document(examId);
        examRef.set(exam).addOnSuccessListener(unused -> {
            WriteBatch batch = db.batch();
            for (Question q : questions) {
                String qId = examRef.collection("questions").document().getId();
                DocumentReference qRef = examRef.collection("questions").document(qId);
                batch.set(qRef, q);
            }
            batch.commit().addOnCompleteListener(listener);
        });
    }

    /**
     * Thêm danh sách studentIds vào Class.
     * Sử dụng FieldValue.arrayUnion để tự động loại bỏ ID trùng lặp.
     *
     * @param examId         ID của Exam
     * @param sessionIdtoAdd Danh sách session ID (String) muốn thêm
     * @param listener       Listener cho kết quả hoàn thành
     */
    public void addSessionId(
            String examId,
            List<String> sessionIdtoAdd,
            OnCompleteListener<Void> listener) {

        DocumentReference docRef = db.collection("exam").document(examId);

        // Sử dụng update với FieldValue.arrayUnion để thêm các ID vào mảng
        // Firestore sẽ tự động bỏ qua các ID đã tồn tại trong mảng studentsId
        docRef.update("sessionId", FieldValue.arrayUnion(sessionIdtoAdd.toArray()))
                .addOnCompleteListener(listener);
    }

    public void deleteExam(String examId, OnCompleteListener<Void> listener) {
        DocumentReference examRef = db.collection("exams").document(examId);
        examRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                String createdBy = snapshot.getString("createdBy");
                if (createdBy != null && createdBy.equals(getCurrentUserId())) {
                    WriteBatch batch = db.batch();
                    examRef.collection("questions").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qSnap) {
                            for (DocumentSnapshot doc : qSnap.getDocuments()) {
                                batch.delete(doc.getReference());
                            }
                            batch.delete(examRef);
                            batch.commit().addOnCompleteListener(listener);
                        }
                    });
                } else {
                    listener.onComplete(Tasks.forException(new Exception("Unauthorized delete")));
                }
            }
        });
    }

    public void fetchAllExams(EventListener<QuerySnapshot> listener) {
        db.collection("exams").addSnapshotListener(listener);
    }

    public void fetchExamQuestions(String examId, EventListener<QuerySnapshot> listener) {
        db.collection("exams").document(examId).collection("questions").addSnapshotListener(listener);
    }

    public void fetchExamDetails(String examId, EventListener<DocumentSnapshot> listener) {
        db.collection("exams").document(examId).addSnapshotListener(listener);
    }

    public interface QuestionCountCallback {
        void onCountFetched(int count);

        void onError(Exception e);
    }

    public void fetchQuestionCount(String examId, QuestionCountCallback callback) {
        db.collection("exams").document(examId).collection("questions")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e("ExamController", "Error fetching question count for exam: " + examId, error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                    return;
                }

                if (querySnapshot != null) {
                    int count = querySnapshot.size();
                    Log.d("ExamController", "Fetched question count for exam " + examId + ": " + count);
                    if (callback != null) {
                        callback.onCountFetched(count);
                    }
                } else {
                    // This case is less likely with addSnapshotListener unless there's an error
                    Log.w("ExamController", "QuerySnapshot is null when fetching count for exam: " + examId);
                    if (callback != null) {
                        callback.onCountFetched(0); // Report 0 count for null snapshot
                    }
                }
            });
    }
}
