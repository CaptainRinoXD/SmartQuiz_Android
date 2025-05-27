package com.example.smartquiz.Model;

import java.util.List;

public class Assessment {
    private String id;
    private String title;
    private String createdBy;
    private String classId;
    private List<StudentInfo> student;

    public Assessment() {
        // Required for Firestore
    }

    public Assessment(String id, String title, String createdBy, String classId) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.classId = classId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public String getClassId() {
        return classId;
    }
    public void setClassId(String classId) {
        this.classId = classId;
    }

    public List<StudentInfo> getStudent() {
        return student;
    }

    public void setStudent(List<StudentInfo> student) {
        this.student = student;
    }

    public static class StudentInfo {
        private String userId;
        private boolean isSelected;

        public StudentInfo() {
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

    
}
