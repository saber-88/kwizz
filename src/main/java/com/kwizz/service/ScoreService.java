package com.kwizz.service;

import com.kwizz.dto.LeaderboardEntry;
import com.kwizz.dto.LeaderboardMessage;
import com.kwizz.entity.Participant;
import com.kwizz.entity.Question;
import com.kwizz.entity.Score;
import com.kwizz.repository.ParticipantRepository;
import com.kwizz.repository.QuestionRepository;
import com.kwizz.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ScoreService {

    private static final int BASE_POINTS = 100;

    private final ScoreRepository scoreRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantRepository participantRepository;

    public ScoreService(ScoreRepository scoreRepository, QuestionRepository questionRepository,
                        ParticipantRepository participantRepository) {
        this.scoreRepository = scoreRepository;
        this.questionRepository = questionRepository;
        this.participantRepository = participantRepository;
    }

    public void recordAnswer(String participantToken, Long questionId, int selectedOption, long timeTakenMs) {
        Participant participant = participantRepository.findBySessionToken(participantToken)
                .orElseThrow(() -> new IllegalArgumentException("Unknown participant."));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("No question with id " + questionId));

        boolean alreadyAnswered = scoreRepository.findByParticipantId(participant.getId()).stream()
                .anyMatch(s -> s.getQuestion().getId().equals(questionId));
        if (alreadyAnswered) {
            return; // ignore a duplicate submission for the same question
        }

        boolean correct = selectedOption == question.getCorrectOption();
        int points = 0;
        if (correct) {
            long timeLimitMs = question.getTimeLimitSeconds() * 1000L;
            long remainingMs = Math.max(0, timeLimitMs - timeTakenMs);
            points = BASE_POINTS + (int) (remainingMs / 100);
        }

        Score score = new Score();
        score.setParticipant(participant);
        score.setQuestion(question);
        score.setSelectedOption(selectedOption);
        score.setCorrect(correct);
        score.setPointsAwarded(points);
        score.setAnswerTimeMs(timeTakenMs);
        scoreRepository.save(score);
    }

    public LeaderboardMessage getLeaderboard(Long sessionId) {
        List<Participant> participants = participantRepository.findBySessionId(sessionId);

        List<LeaderboardEntry> entries = participants.stream()
                .map(p -> {
                    int total = scoreRepository.findByParticipantId(p.getId()).stream()
                            .mapToInt(Score::getPointsAwarded)
                            .sum();
                    return new LeaderboardEntry(p.getNickname(), total);
                })
                .sorted(Comparator.comparingInt(LeaderboardEntry::getScore).reversed())
                .toList();

        return new LeaderboardMessage(entries);
    }
}