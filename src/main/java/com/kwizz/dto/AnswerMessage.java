package com.kwizz.dto;

public class AnswerMessage {
    private Long questionId;
    private String participantToken;
    private int selectedOption;
    private int answerTimeMs;

    public AnswerMessage() {}

    public String getParticipantToken() {
        return participantToken;
    }

    public void setParticipantToken(String participantToken) {
        this.participantToken = participantToken;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public int getTimeTakenMs() {
        return answerTimeMs;
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

    public void setTimeTakenMs(int timeTakenMs) {
        this.answerTimeMs = timeTakenMs;
    }

}
