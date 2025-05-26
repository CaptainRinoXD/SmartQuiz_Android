package com.example.smartquiz.Model;

import java.util.Date;
import java.util.List;

public class Submission {
    private String examId;
    private String studentId;
    private Date submittedAt;
    private float score;
    private String assessmentId;
    private List<SubmissionAnswer> answers;
    private int duration;

    public Submission(String examId, String studentId, Date submittedAt, float score, String assessmentId,
            List<SubmissionAnswer> answers, int duration) {
        this.examId = examId;
        this.studentId = studentId;
        this.submittedAt = submittedAt;
        this.score = score;
        this.assessmentId = assessmentId;
        this.answers = answers;
        this.duration = duration;
    }

    public Submission() {
        // Required for Firestore
    }

    // Getters and setters

    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public List<SubmissionAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmissionAnswer> answers) {
        this.answers = answers;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
