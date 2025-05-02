package com.example.smartquiz.Model;

import java.util.Date;

public class Exam {
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

    public String title;
    public String createdBy;
    public Date createdAt;

    public Exam() {}

    public Exam(String title, String createdBy, Date createdAt) {
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

