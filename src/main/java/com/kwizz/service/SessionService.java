package com.kwizz.service;

import com.kwizz.entity.Quiz;
import com.kwizz.entity.QuizSession;
import com.kwizz.repository.QuizRepository;
import com.kwizz.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class SessionService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final QuizSessionRepository sessionRepo;
    private final QuizRepository quizRepo;

    public SessionService(QuizSessionRepository sessionRepo, QuizRepository quizRepo) {
        this.sessionRepo = sessionRepo;
        this.quizRepo = quizRepo;
    }

    public QuizSession startSession(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("No quiz with id " + quizId));
        QuizSession session = new QuizSession(quiz, generateUniqueJoinCode());
        return sessionRepo.save(session);
    }


    @Transactional(readOnly = true)
    public QuizSession getSession(Long sessionId){
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session with id " + sessionId));
        session.getQuiz().getQuestions().size();

        return session;
    }

    public QuizSession getSessionByJoinCode(String joinCode){
        return sessionRepo.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("No session with join code " + joinCode));

    }

    @Transactional
    public QuizSession nextQuestion(Long sessionId){
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session with id "+ sessionId));

        int totalQuestions = session.getQuiz().getQuestions().size();
        if(session.getStatus() == QuizSession.Status.WAITING){
            session.setStatus(QuizSession.Status.IN_PROGRESS);
            session.setCurrentQuestionIndex(0);
        } else if (session.getStatus() == QuizSession.Status.IN_PROGRESS) {
            int next = session.getCurrentQuestionIndex() + 1;
            if (next >= totalQuestions){
                session.setStatus(QuizSession.Status.FINISHED);
            }
            else{
                session.setCurrentQuestionIndex(next);
            }
        }
        return sessionRepo.save(session);
    }

    public QuizSession stopSession(Long sessionId){
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session with id "+ sessionId));
        session.setStatus(QuizSession.Status.FINISHED);
        return sessionRepo.save(session);
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            code = randomCode();
        } while (sessionRepo.findByJoinCode(code).isPresent());
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }



}
