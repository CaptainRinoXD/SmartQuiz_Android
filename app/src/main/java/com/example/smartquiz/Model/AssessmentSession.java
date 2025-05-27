package com.example.smartquiz.Model;

import java.util.Date;
import java.util.List;

public class AssessmentSession {
    private String title;
    private Date time;
    private List<String> studentsInSession;

    public AssessmentSession() {
        // Required by Firestore
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<String> getStudentsInSession() {
        return studentsInSession;
    }

    public void setStudentsInSession(List<String> studentsInSession) {
        this.studentsInSession = studentsInSession;
    }
}
