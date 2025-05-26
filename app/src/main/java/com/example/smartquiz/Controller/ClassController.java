package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.Class;
import com.google.android.gms.tasks.OnCompleteListener;

public class ClassController extends BaseController{
    public void createClass(Class newClass, OnCompleteListener<Void> listener) {
        String classId = db.collection("classes").document().getId();
        newClass.setId(classId);
        db.collection("classes").document(classId).set(newClass).addOnCompleteListener(listener);
    }

    public void updateClass(Class updatedClass, OnCompleteListener<Void> listener) {
        db.collection("classes").document(updatedClass.getId()).set(updatedClass).addOnCompleteListener(listener);
    }

    public void updateClassStudents(Class updatedClass, OnCompleteListener<Void> listener) {
        db.collection("classes").document(updatedClass.getId())
                .update("studentsId", updatedClass.getStudentsId())
                .addOnCompleteListener(listener);
    }

    public void deleteClass(String classId, OnCompleteListener<Void> listener) {
        db.collection("classes").document(classId).delete().addOnCompleteListener(listener);
    }


}