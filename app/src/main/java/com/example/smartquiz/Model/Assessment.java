package com.example.smartquiz.Model;

import java.util.List;

public class Assessment {
    private String id;
    private String title;
    private String createdBy;
    private String classId;

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
}
