package com.kwizz.repository;

import com.kwizz.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findBySessionToken(String sessionToken);
    List<Participant> findBySessionId(Long sessionId);
    // ParticipantRepository
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    delete from Participant p
    where p.session.id in (
        select qs.id from QuizSession qs
        where qs.quiz.id = :quizId
    )
    """)
    void deleteAllByQuizId(@Param("quizId") Long quizId);
}
