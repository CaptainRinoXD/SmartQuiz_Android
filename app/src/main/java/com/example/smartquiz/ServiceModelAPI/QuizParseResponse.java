package com.example.smartquiz.ServiceModelAPI;

import com.example.smartquiz.Model.Question;

import java.util.List;

public class QuizParseResponse {
    public boolean success;
    public String message;
    public int totalFound;
    public int validCount;
    public List<Question> questions;
    public List<ValidationWarning> warnings;
    public FormatExample formatExample;

    public QuizParseResponse() {
        // Required for Gson
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<ValidationWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ValidationWarning> warnings) {
        this.warnings = warnings;
    }

    public FormatExample getFormatExample() {
        return formatExample;
    }

    public void setFormatExample(FormatExample formatExample) {
        this.formatExample = formatExample;
    }
}
