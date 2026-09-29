package com.kwizz.dto;

import com.kwizz.entity.Question;

public class AnswerMessage {
    private Long questionId;
    private int selectedOption;
    private int timeTaken;

    public AnswerMessage() {}


    public Long getQuestionId() {
        return questionId;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }

}
