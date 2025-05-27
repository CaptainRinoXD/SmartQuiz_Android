package com.example.smartquiz.Controller;
import com.example.smartquiz.Model.Assessment;
import com.example.smartquiz.Model.AssessmentSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssessmentController extends BaseController{
    public void createAssessment(Assessment assessment, OnCompleteListener<Void> listener) {
        String id = db.collection("Assessment").document().getId();
        db.collection("Assessment").document(id).set(assessment)
                .addOnCompleteListener(listener);
    }

    public void addSession(String assessmentId, AssessmentSession session,
            OnSuccessListener<DocumentReference> listener) {
        db.collection("Assessment")
                .document(assessmentId)
                .collection("session")
                .add(session)
                .addOnSuccessListener(listener);
    }

    public void getAllAssessments(OnSuccessListener<QuerySnapshot> listener) {
        db.collection("Assessment").get().addOnSuccessListener(listener);
    }

    // Lấy tất cả session theo assessment
    public void getAllSessions(String assessmentId, OnSuccessListener<QuerySnapshot> listener) {
        db.collection("Assessment")
                .document(assessmentId)
                .collection("session")
                .get()
                .addOnSuccessListener(listener);
    }

    /**
     * Thêm danh sách sinh viên vào Assessment. Chỉ thêm những sinh viên có userId
     * chưa tồn tại.
     * 
     * @param assessmentId  ID của Assessment
     * @param studentsToAdd Danh sách StudentInfo muốn thêm
     * @param listener      Listener cho kết quả hoàn thành
     */
    public void addStudents(
            String assessmentId,
            List<Assessment.StudentInfo> studentsToAdd,
            OnCompleteListener<Void> listener) {

        DocumentReference docRef = db.collection("Assessment").document(assessmentId);

        db.<Void>runTransaction(transaction -> {
            Assessment assessment = transaction.get(docRef).toObject(Assessment.class);
            if (assessment == null)
                return null; // Assessment doesn't exist

            List<Assessment.StudentInfo> currentStudents = assessment.getStudent();
            if (currentStudents == null) {
                currentStudents = new ArrayList<>();
            }

            // Tạo set các userId hiện có để kiểm tra trùng lặp nhanh hơn
            Set<String> existingUserIds = currentStudents.stream()
                    .map(Assessment.StudentInfo::getUserId)
                    .collect(Collectors.toSet());

            // Lọc ra những sinh viên cần thêm (userId chưa tồn tại)
            List<Assessment.StudentInfo> studentsToAddFiltered = studentsToAdd.stream()
                    .filter(student -> !existingUserIds.contains(student.getUserId()))
                    .collect(Collectors.toList());

            if (!studentsToAddFiltered.isEmpty()) {
                currentStudents.addAll(studentsToAddFiltered); // Thêm các sinh viên mới vào danh sách hiện tại
                transaction.update(docRef, "student", currentStudents); // Cập nhật lại trường student
            }

            return null;
        }).addOnCompleteListener(listener);
    }

    // Lấy tất cả sinh viên chưa được chọn trong assessment
    public void getUnselectedStudents(String assessmentId, OnSuccessListener<List<Assessment.StudentInfo>> listener) {
        db.collection("Assessment").document(assessmentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Assessment.StudentInfo> result = new ArrayList<>();
                    if (snapshot.exists()) {
                        Assessment assess = snapshot.toObject(Assessment.class);
                        if (assess != null && assess.getStudent() != null) {
                            for (Assessment.StudentInfo s : assess.getStudent()) {
                                if (!s.isSelected()) result.add(s);
                            }
                        }
                    }
                    listener.onSuccess(result);
                });
    }

    // Cập nhật isSelected cho sinh viên trong Assessment, nếu ko có thì thêm mới
    public void updateStudentSelection(
            String assessmentId,
            String userId,
            boolean isSelected,
            OnCompleteListener<Void> listener) {
        DocumentReference docRef =db.collection("Assessment").document(assessmentId);

        db.<Void>runTransaction(transaction -> {

            Assessment assess = transaction.get(docRef).toObject(Assessment.class);
            if (assess == null) return null;

            List<Assessment.StudentInfo> list = assess.getStudent();
            if (list == null) list = new ArrayList<>();

            boolean found = false;
            for (Assessment.StudentInfo s : list) {
                if (s.getUserId().equals(userId)) {
                    s.setSelected(isSelected);
                    found = true;
                    break;
                }
            }
            // Nếu chưa có userId, thêm mới
            if (!found) {
                Assessment.StudentInfo s = new Assessment.StudentInfo();
                s.setUserId(userId);
                s.setSelected(isSelected);
                list.add(s);
            }

            transaction.update(docRef, "student", list);
            return null;
        }).addOnCompleteListener(listener);
    }

    /**
     * Xóa sinh viên trong assessment.
     * @param assessmentId ID của Assessment
     * @param userId       userId muốn xóa (có thể 1 phần tử)
     */
    public void removeStudent(
            String assessmentId,
            String userId,
            OnCompleteListener<Void> listener) {

        DocumentReference docRef = db.collection("Assessment").document(assessmentId);

        db.<Void>runTransaction(transaction -> {
            Assessment assessment = transaction.get(docRef).toObject(Assessment.class);
            if (assessment == null) return null;

            List<Assessment.StudentInfo> students = assessment.getStudent();
            if (students != null) {
                students.removeIf(s -> s.getUserId().equals(userId));
            }
            transaction.update(docRef, "student", students);
            return null;
        }).addOnCompleteListener(listener);
    }


    /// Assessment Session Section
    /**
     * Thêm các userId vào studentsInSession (chỉ thêm những ID hợp lệ).
     * @param assessmentId ID của Assessment
     * @param sessionId    ID của session
     * @param userIds      Danh sách userId muốn thêm (có thể 1 phần tử)
     */
    public void addStudentsToSession(
            String assessmentId,
            String sessionId,
            List<String> userIds,
            OnCompleteListener<Void> listener) {

        DocumentReference assessRef = db.collection("Assessment").document(assessmentId);
        DocumentReference sessionRef = assessRef.collection("session").document(sessionId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1) Đọc Assessment để lấy danh sách student hợp lệ
            Assessment assessment = transaction.get(assessRef).toObject(Assessment.class);
            if (assessment == null || assessment.getStudent() == null)
                return null;

            // Tạo set các userId hợp lệ
            Set<String> valid = assessment.getStudent().stream()
                    .map(Assessment.StudentInfo::getUserId)
                    .collect(Collectors.toSet());

            // Giữ lại chỉ các userId nằm trong Assessment
            List<String> filtered = userIds.stream()
                    .filter(valid::contains)
                    .collect(Collectors.toList());
            if (filtered.isEmpty())
                return null; // không có gì để thêm

            // 2) Thêm vào studentsInSession bằng arrayUnion
            transaction.update(
                    sessionRef,
                    "studentsInSession",
                    FieldValue.arrayUnion(filtered.toArray()));
            return null;
        }).addOnCompleteListener(listener);
    }

    /**
     * Xoá các userId khỏi studentsInSession.
     * @param assessmentId  ID của Assessment
     * @param sessionId     ID của session
     * @param userIds       Danh sách userId muốn xoá (có thể 1 phần tử)
     */
    public void removeStudentsFromSession(
            String assessmentId,
            String sessionId,
            List<String> userIds,
            OnCompleteListener<Void> listener) {

        DocumentReference assessRef  = db.collection("Assessment").document(assessmentId);
        DocumentReference sessionRef = assessRef.collection("session").document(sessionId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Đảm bảo session tồn tại – nếu cần
            transaction.get(sessionRef);

            transaction.update(
                    sessionRef,
                    "studentsInSession",
                    FieldValue.arrayRemove(userIds.toArray())
            );
            return null;
        }).addOnCompleteListener(listener);
    }


}
