package com.kwizz.service;

import com.kwizz.entity.Participant;
import com.kwizz.entity.QuizSession;
import com.kwizz.exception.ResourceNotFoundException;
import com.kwizz.repository.ParticipantRepository;
import com.kwizz.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QuizSessionRepository sessionRepository;

    public ParticipantService(ParticipantRepository participantRepository, QuizSessionRepository sessionRepository) {
        this.participantRepository = participantRepository;
        this.sessionRepository = sessionRepository;
    }
    public boolean nicknameTaken(Long sessionId, String name){
        return participantRepository.findBySessionId(sessionId).stream()
                .anyMatch(p -> p.getNickname().equals(name));
    }
    public Participant joinSession(String joinCode, String nickname) {

        QuizSession session = sessionRepository.findByJoinCode(joinCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No session found with that join code."));

        if (nicknameTaken(session.getId(), nickname)){
            throw new IllegalArgumentException("NickName is already taken.");
        }
        if(session.getStatus() != QuizSession.Status.WAITING){
            throw new IllegalArgumentException("You are late , the quiz already started.");
        }

        String token = UUID.randomUUID().toString();
        Participant participant = new Participant(nickname, token, session);
        return participantRepository.save(participant);
    }

    public Participant getByToken(String token) {
        return participantRepository.findBySessionToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown participant."));
    }

    public List<String> getNickNames(Long sessionId){
        return participantRepository.findBySessionId(sessionId).stream()
                .map(Participant::getNickname)
                .toList();
    }
}