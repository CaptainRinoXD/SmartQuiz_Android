package com.example.smartquiz.Model;

public class User {
    private String uid;
    private String name;
    private String email;
    private String role;
    private long totalTime = 0;
    private int totalExams = 0;

    public User() {
    }

    public User(String uid, String name, String email, String role, long totalTime, int totalExams) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.totalTime = totalTime;
        this.totalExams = totalExams;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(int totalExams) {
        this.totalExams = totalExams;
    }
}