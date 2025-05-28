package com.example.smartquiz.Model;

import java.util.List;

public class Class {
    private String id;
    private String name;
    public String createdBy;
    private List<String> studentsId;
    public Class(List<String> studentsId, String createdBy, String name, String id) {
        this.studentsId = studentsId;
        this.createdBy = createdBy;
        this.name = name;
        this.id = id;
    }



    public Class() {
        // Required for Firestore
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getStudentsId() {
        return studentsId;
    }

    public void setStudentsId(List<String> studentsId) {
        this.studentsId = studentsId;
    }
}
