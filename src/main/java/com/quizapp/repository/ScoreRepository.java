package com.quizapp.repository;

import com.quizapp.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByParticipantId(Long participantId);
}
