package com.kwizz.dto;

public class QuestionMessage {

    private Long questionId;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int timeLimitSeconds;
    private int questionNumber;
    private int totalQuestions;

    public QuestionMessage() {}

    public QuestionMessage(Long questionId, String text, String optionA, String optionB,
                           String optionC, String optionD, int timeLimitSeconds,
                           int questionNumber, int totalQuestions) {
        this.questionId = questionId;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.timeLimitSeconds = timeLimitSeconds;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
    }

    public Long getQuestionId() { return questionId; }
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public int getQuestionNumber() { return questionNumber; }
    public int getTotalQuestions() { return totalQuestions; }
}