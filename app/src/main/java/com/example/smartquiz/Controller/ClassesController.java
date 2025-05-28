package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.Class;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassesController extends BaseController{
    public void createClass(Class newClass, OnCompleteListener<Void> listener) {
        newClass.setCreatedBy(getCurrentUserId());
        String classId = db.collection("classes").document().getId();
        newClass.setId(classId);
        db.collection("classes").document(classId).set(newClass).addOnCompleteListener(listener);
    }

    public void updateClass(Class updatedClass, OnCompleteListener<Void> listener) {
        db.collection("classes").document(updatedClass.getId()).set(updatedClass).addOnCompleteListener(listener);
    }

    public void getClassByCreatedBy(String userId, OnCompleteListener<QuerySnapshot> listener) {
        Query query = db.collection("classes").whereEqualTo("createdBy", userId);
        query.get().addOnCompleteListener(listener);
    }

    public void updateClassStudents(Class updatedClass, OnCompleteListener<Void> listener) {
        db.collection("classes").document(updatedClass.getId())
                .update("studentsId", updatedClass.getStudentsId())
                .addOnCompleteListener(listener);
    }

    public void deleteClass(String classId, OnCompleteListener<Void> listener) {
        db.collection("classes").document(classId).delete().addOnCompleteListener(listener);
    }

    /**
     * Thêm danh sách studentIds vào Class sử dụng Transaction (Client-side
     * read-modify-write).
     * Kiểm tra trùng lặp và thêm sinh viên mới một cách atomic.
     *
     * @param classId         ID của Class
     * @param studentIdsToAdd Danh sách student ID (String) muốn thêm
     * @param listener        Listener cho kết quả hoàn thành
     */
    public void addStudentsTransactionally(
            String classId,
            List<String> studentIdsToAdd,
            OnCompleteListener<Void> listener) {

        DocumentReference docRef = db.collection("classes").document(classId);

        db.<Void>runTransaction(transaction -> {
            // 1) Đọc dữ liệu hiện tại của Class
            Class currClass = transaction.get(docRef).toObject(Class.class);
            if (currClass == null) {
                // Document doesn't exist, or couldn't be converted
                return null;
            }

            List<String> currentStudents = currClass.getStudentsId();
            if (currentStudents == null) {
                currentStudents = new ArrayList<>();
            }

            // 2) Tạo set các userId hiện có để kiểm tra trùng lặp nhanh hơn
            Set<String> existingUserIds = new HashSet<>(currentStudents);

            // 3) Lọc ra những sinh viên cần thêm (userId chưa tồn tại)
            List<String> studentsToAddFiltered = studentIdsToAdd.stream()
                    .filter(studentId -> !existingUserIds.contains(studentId))
                    .collect(Collectors.toList());

            // 4) Nếu có sinh viên mới cần thêm, thêm vào danh sách và cập nhật
            if (!studentsToAddFiltered.isEmpty()) {
                currentStudents.addAll(studentsToAddFiltered); // Thêm các sinh viên mới vào danh sách hiện tại
                // 5) Cập nhật lại trường studentsId trong transaction
                transaction.update(docRef, "studentsId", currentStudents);
            }

            // Transaction thành công (hoặc không có gì để cập nhật)
            return null;
        }).addOnCompleteListener(listener);
    }

    /**
     * Thêm danh sách studentIds vào Class.
     * Sử dụng FieldValue.arrayUnion để tự động loại bỏ ID trùng lặp.
     *
     * @param classId         ID của Class
     * @param studentIdsToAdd Danh sách student ID (String) muốn thêm
     * @param listener        Listener cho kết quả hoàn thành
     */
    public void addStudents(
            String classId,
            List<String> studentIdsToAdd,
            OnCompleteListener<Void> listener) {

        DocumentReference docRef = db.collection("classes").document(classId);

        // Sử dụng update với FieldValue.arrayUnion để thêm các ID vào mảng
        // Firestore sẽ tự động bỏ qua các ID đã tồn tại trong mảng studentsId
        docRef.update("studentsId", FieldValue.arrayUnion(studentIdsToAdd.toArray()))
                .addOnCompleteListener(listener);
    }


}