package com.example.smartquiz.Model;

import java.util.Date;
import java.util.List;

public class Exam {
    public String title;
    public String createdBy;
    public Date createdAt;
    public int duration;
    private List<String> sessionId;

    
    
    public Exam(String title, String createdBy, Date createdAt, int duration, List<String> sessionId) {
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.duration = duration;
        this.sessionId = sessionId;
    }

    public Exam() {}

    public List<String> getSessionId() {
        return sessionId;
    }

    public void setSessionId(List<String> sessionId) {
        this.sessionId = sessionId;
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

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}

