package com.exposure.DTOs.game;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResponse {
    public String answer;
    public int remainingQuestions;

    public QuestionResponse(String answer, int remainingQuestions) {
        this.answer = answer;
        this.remainingQuestions = remainingQuestions;
    }
}
