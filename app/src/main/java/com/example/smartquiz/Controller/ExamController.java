package com.example.smartquiz.Controller;

import android.util.Log;

import com.example.smartquiz.Model.AssessmentSession;
import com.example.smartquiz.Model.Exam;
import com.example.smartquiz.Model.Question;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExamController extends BaseController {
    public void createExam(Exam exam, List<Question> questions, OnCompleteListener<Void> listener) {
        String examId = db.collection("exams").document().getId();
        exam.setCreatedAt(new Date());
        exam.setCreatedBy(getCurrentUserId());
        exam.setId(examId);

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

    @FunctionalInterface
    public interface SessionResultCallback {
        void onResult(List<AssessmentSession> sessions, Exception error);
    }

    public void fetchSessionsForCurrentStudent(SessionResultCallback callback) {
        String userId = getCurrentUserId();

        db.collectionGroup("session")
                .whereArrayContains("studentsInSession", userId)
                .get()
                .addOnSuccessListener(sessionSnap -> {
                    List<AssessmentSession> sessions = new ArrayList<>();

                    for (DocumentSnapshot doc : sessionSnap.getDocuments()) {
                        AssessmentSession session = doc.toObject(AssessmentSession.class);
                        if (session != null) {
                            session.setId(doc.getId()); // Set document ID
                            sessions.add(session);
                        }
                    }

                    callback.onResult(sessions, null);
                })
                .addOnFailureListener(e -> callback.onResult(null, e));
    }

    @FunctionalInterface
    public interface ExamResultCallback {
        void onResult(List<Exam> exams, Exception error);
    }

    public void fetchExamsBySessionId(String sessionId, ExamResultCallback callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onResult(new ArrayList<>(), null);
            return;
        }

        db.collection("exams")
                .whereArrayContains("sessionId", sessionId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Exam> exams = new ArrayList<>();

                    for (DocumentSnapshot d : querySnapshot) {
                        Exam exam = d.toObject(Exam.class);
                        if (exam != null) {
                            exam.setId(d.getId()); // Set document ID if needed
                            exams.add(exam);
                        }
                    }

                    callback.onResult(exams, null);
                })
                .addOnFailureListener(e -> callback.onResult(null, e));
    }

//    @FunctionalInterface
//    public interface ExamResultCallback {
//        void onResult(List<Exam> exams, Exception error);
//    }
//
    public void fetchExamsForCurrentStudent(ExamResultCallback callback) {
        String userId = getCurrentUserId();

        // Query all subcollections named "session"
        db.collectionGroup("session")
                .whereArrayContains("studentsInSession", userId)
                .get()
                .addOnSuccessListener(sessionSnap -> {
                    List<AssessmentSession> sessions = new ArrayList<>();
                    List<Task<QuerySnapshot>> examTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : sessionSnap.getDocuments()) {
                        AssessmentSession session = doc.toObject(AssessmentSession.class);
                        if (session != null) {
                            session.setId(doc.getId()); // Make sure to set the ID
                            sessions.add(session);

                            Task<QuerySnapshot> t = db.collection("exams")
                                    .whereArrayContains("sessionId", session.getId())
                                    .get();
                            examTasks.add(t);
                        }
                    }

                    if (examTasks.isEmpty()) {
                        callback.onResult(new ArrayList<>(), null);
                        return;
                    }

                    Tasks.whenAllSuccess(examTasks)
                            .addOnSuccessListener(results -> {
                                List<Exam> allExams = new ArrayList<>();
                                for (Object o : results) {
                                    QuerySnapshot qs = (QuerySnapshot) o;
                                    for (DocumentSnapshot d : qs) {
                                        Exam exam = d.toObject(Exam.class);
                                        if (exam != null) {
                                            allExams.add(exam);
                                        }
                                    }
                                }
                                callback.onResult(allExams, null);
                            })
                            .addOnFailureListener(e -> callback.onResult(null, e));
                })
                .addOnFailureListener(e -> callback.onResult(null, e));
    }

    public void fetchExamQuestions(String examId, EventListener<QuerySnapshot> listener) {
        db.collection("exams").document(examId).collection("questions").addSnapshotListener(listener);
    }

    public void fetchExamDetails(String examId, EventListener<DocumentSnapshot> listener) {
        db.collection("exams").document(examId).addSnapshotListener(listener);
    }

    public void fetchExamDocumentByAssessmentId(String assessmentId, EventListener<QuerySnapshot> listener) {
        db.collection("exams").whereEqualTo("assessment_id", assessmentId).addSnapshotListener(listener);
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

    public void updateExam(
            String examId,
            Exam updatedExam,
            List<Question> updatedQuestions,
            OnCompleteListener<Void> listener) {

        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onComplete(Tasks.forException(new Exception("User not authenticated")));
            return;
        }

        DocumentReference examRef = db.collection("exams").document(examId);

        examRef.get().addOnSuccessListener(examSnapshot -> {
            if (!examSnapshot.exists()) {
                listener.onComplete(Tasks.forException(new Exception("Exam not found")));
                return;
            }
            String createdBy = examSnapshot.getString("createdBy");
            if (createdBy == null || !createdBy.equals(userId)) {
                listener.onComplete(Tasks.forException(new Exception("Unauthorized update")));
                return;
            }

            examRef.collection("questions").get().addOnSuccessListener(currentQuestionsSnapshot -> {

                WriteBatch batch = db.batch();

                Set<String> currentQuestionIds = new HashSet<>();
                for (DocumentSnapshot doc : currentQuestionsSnapshot.getDocuments()) {
                    currentQuestionIds.add(doc.getId());
                }

                Set<String> processedUpdatedIds = new HashSet<>();
                for (Question question : updatedQuestions) {
                    String questionId = question.getId();
                    if (questionId != null && !questionId.isEmpty() && currentQuestionIds.contains(questionId)) {
                        DocumentReference qRef = examRef.collection("questions").document(questionId);
                        batch.set(qRef, question);
                        processedUpdatedIds.add(questionId);
                        Log.d("ExamController", "Adding update to batch for question ID: " + questionId);
                    } else {
                        String newQId = examRef.collection("questions").document().getId();
                        question.setId(newQId);
                        DocumentReference newQRef = examRef.collection("questions").document(newQId);
                        batch.set(newQRef, question);
                        processedUpdatedIds.add(newQId);
                        Log.d("ExamController", "Adding new question to batch with ID: " + newQId);
                    }
                }

                for (String idToDelete : currentQuestionIds) {
                    if (!processedUpdatedIds.contains(idToDelete)) {
                        DocumentReference qRefToDelete = examRef.collection("questions").document(idToDelete);
                        batch.delete(qRefToDelete);
                        Log.d("ExamController", "Adding delete to batch for question ID: " + idToDelete);
                    }
                }

                Map<String, Object> examUpdates = new HashMap<>();
                examUpdates.put("title", updatedExam.getTitle());
                examUpdates.put("duration", updatedExam.getDuration());
                examUpdates.put("sessionId", updatedExam.getSessionId());
                examUpdates.put("assessment_id", updatedExam.getAssessment_id());

                batch.update(examRef, examUpdates);
                Log.d("ExamController", "Adding main exam update to batch for exam ID: " + examId);

                batch.commit()
                        .addOnCompleteListener(listener);

            }).addOnFailureListener(e -> {
                listener.onComplete(Tasks.forException(e));
                Log.e("ExamController", "Failed to fetch current questions for exam: " + examId, e);
            });

        }).addOnFailureListener(e -> {
            listener.onComplete(Tasks.forException(e));
            Log.e("ExamController", "Failed to fetch exam details for update: " + examId, e);
        });
    }
}
