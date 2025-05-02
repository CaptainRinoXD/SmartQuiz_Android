package com.example.smartquiz.Model;

import java.util.List;


public class Question {
    public String questionText;
    public List<Answer> answers;

    public Question() {}

    public Question(String questionText, List<Answer> answers) {
        this.questionText = questionText;
        this.answers = answers;
    }
}

