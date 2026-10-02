package com.kwizz.repository;

import com.kwizz.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByParticipantId(Long participantId);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    delete from Score s
    where s.participant.id in (
        select p.id from Participant p
        where p.session.quiz.id = :quizId
    )
    """)
    void deleteAllByQuizId(@Param("quizId") Long quizId);
}
