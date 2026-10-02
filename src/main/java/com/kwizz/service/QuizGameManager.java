package com.kwizz.service;

import com.kwizz.dto.LeaderboardMessage;
import com.kwizz.entity.Question;
import com.kwizz.entity.QuizSession;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
public class QuizGameManager {

    private final SessionService sessionService;
    private final ScoreService scoreService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final Map<Long, ScheduledFuture<?>> pendingTimers = new ConcurrentHashMap<>();
    // true while a session is frozen on the leaderboard, waiting for the
    // host to click "Next question" again to actually move on.
    private final Map<Long, Boolean> showingLeaderboard = new ConcurrentHashMap<>();

    public QuizGameManager(SessionService sessionService, ScoreService scoreService,
                           SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.scoreService = scoreService;
        this.messagingTemplate = messagingTemplate;
    }

    public boolean isShowingLeaderboard(Long sessionId) {
        return showingLeaderboard.getOrDefault(sessionId, false);
    }

    // Called every time the host clicks "Next question" - behavior depends
    // entirely on which phase the session is currently in.
    public void handleNextClick(Long sessionId) {
        QuizSession currentState = sessionService.getSession(sessionId);

        if (currentState.getStatus() == QuizSession.Status.WAITING) {
            // Very first click ever for this session: go straight to
            // question 1, nothing to show a leaderboard for yet.
            doAdvance(sessionId);
            return;
        }

        if (isShowingLeaderboard(sessionId)) {
            showingLeaderboard.put(sessionId, false);
            doAdvance(sessionId);
        } else {
            revealLeaderboard(sessionId);
        }
    }

    // Called automatically when a question's own timer runs out with
    // nobody having clicked anything - same outcome as an early click.
    private void onQuestionTimeout(Long sessionId) {
        revealLeaderboard(sessionId);
    }

    private void revealLeaderboard(Long sessionId) {
        cancelPendingTimer(sessionId);
        QuizSession currentState = sessionService.getSession(sessionId);
        if (currentState.getStatus() == QuizSession.Status.IN_PROGRESS) {
            broadcastLeaderboard(sessionId);
            showingLeaderboard.put(sessionId, true);
            // No timer scheduled here on purpose - it now stays frozen
            // until the host explicitly clicks Next again.
        }
    }

    public void stopQuiz(Long sessionId) {
        cancelPendingTimer(sessionId);
        showingLeaderboard.remove(sessionId);
        QuizSession currentState = sessionService.getSession(sessionId);
        if (currentState.getStatus() == QuizSession.Status.IN_PROGRESS) {
            broadcastLeaderboard(sessionId);
        }
        QuizSession session = sessionService.stopSession(sessionId);
        sessionService.broadcastCurrentState(sessionId, session);
    }

    private void doAdvance(Long sessionId) {
        pendingTimers.remove(sessionId);
        QuizSession session = sessionService.nextQuestion(sessionId);
        sessionService.broadcastCurrentState(sessionId, session);

        if (session.getStatus() == QuizSession.Status.IN_PROGRESS) {
            Question question = session.getQuiz().getQuestions().get(session.getCurrentQuestionIndex());
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> onQuestionTimeout(sessionId),
                    question.getTimeLimitSeconds(),
                    TimeUnit.SECONDS
            );
            pendingTimers.put(sessionId, future);
        } else {
            showingLeaderboard.remove(sessionId); // FINISHED - nothing left to track
        }
    }

    private void cancelPendingTimer(Long sessionId) {
        ScheduledFuture<?> existing = pendingTimers.remove(sessionId);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    private void broadcastLeaderboard(Long sessionId) {
        LeaderboardMessage leaderboard = scoreService.getLeaderboard(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/leaderboard", leaderboard);
    }
}