package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Question;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
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
}
