package com.quizapp.repository;

import com.quizapp.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findBySessionToken(String sessionToken);
    List<Participant> findBySessionId(Long sessionId);
}
