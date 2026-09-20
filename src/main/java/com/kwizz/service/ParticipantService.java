package com.kwizz.service;

import com.kwizz.entity.Participant;
import com.kwizz.entity.QuizSession;
import com.kwizz.repository.ParticipantRepository;
import com.kwizz.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QuizSessionRepository sessionRepository;

    public ParticipantService(ParticipantRepository participantRepository, QuizSessionRepository sessionRepository) {
        this.participantRepository = participantRepository;
        this.sessionRepository = sessionRepository;
    }

    public Participant joinSession(String joinCode, String nickname) {
        // Join codes are generated uppercase (see SessionService) -
        // normalize input so "ab12cd" and "AB12CD" both work.
        QuizSession session = sessionRepository.findByJoinCode(joinCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No session found with that join code."));

        // No login for participants, so a random token is their entire
        // identity for the rest of the session - stored in their
        // HttpSession by JoinController after this returns.
        String token = UUID.randomUUID().toString();
        Participant participant = new Participant(nickname, token, session);
        return participantRepository.save(participant);
    }

    public Participant getByToken(String token) {
        return participantRepository.findBySessionToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Unknown participant."));
    }
}