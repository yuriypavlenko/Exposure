package com.exposure.DTOs.game;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResponse {
    public String answer;
    public int questionsLeft;

    public QuestionResponse(String answer, int questionsLeft) {
        this.answer = answer;
        this.questionsLeft = questionsLeft;
    }
}
