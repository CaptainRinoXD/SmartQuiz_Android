package com.example.smartquiz.Model;

import java.util.List;

public class Class {
    private String id;
    private String name;
    public String createdBy;
    private String description;
    private List<String> studentsId;
   
    public Class(String id, String name, String createdBy, String description, List<String> studentsId) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.description = description;
        this.studentsId = studentsId;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<String> getStudentsId() {
        return studentsId;
    }

    public void setStudentsId(List<String> studentsId) {
        this.studentsId = studentsId;
    }
}
