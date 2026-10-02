package com.kwizz.repository;

import com.kwizz.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByJoinCode(String joinCode);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from QuizSession qs where qs.quiz.id = :quizId")
    void deleteAllByQuizId(@Param("quizId") Long quizId);
}
