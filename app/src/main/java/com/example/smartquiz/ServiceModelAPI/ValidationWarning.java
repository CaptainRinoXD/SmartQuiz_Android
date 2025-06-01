package com.example.smartquiz.ServiceModelAPI;

import java.util.List;

public class ValidationWarning {
    public int questionNumber;
    public List<String> errors;

    public ValidationWarning() {
        // Required for Gson
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}