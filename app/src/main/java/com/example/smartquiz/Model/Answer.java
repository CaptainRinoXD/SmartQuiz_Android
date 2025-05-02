package com.example.smartquiz.Model;

public class Answer {
    public String text;
    public boolean isCorrect;

    public Answer() {}

    public Answer(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }
}
