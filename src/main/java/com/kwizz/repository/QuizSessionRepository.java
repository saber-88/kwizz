package com.kwizz.repository;

import com.kwizz.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByJoinCode(String joinCode);
}
