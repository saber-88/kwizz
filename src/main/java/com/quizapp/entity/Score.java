package com.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private int selectedOption; // 0-3, or -1 if no answer / timed out

    private boolean correct;

    // points awarded for THIS question (speed + correctness bonus can be
    // computed in the service layer and stored here)
    private int pointsAwarded;

    // milliseconds taken to answer, from when the question was pushed
    private long answerTimeMs;

    public Score() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public int getSelectedOption() { return selectedOption; }
    public void setSelectedOption(int selectedOption) { this.selectedOption = selectedOption; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public int getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public long getAnswerTimeMs() { return answerTimeMs; }
    public void setAnswerTimeMs(long answerTimeMs) { this.answerTimeMs = answerTimeMs; }
}
