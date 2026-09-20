package com.kwizz.controller;

import com.kwizz.entity.Participant;
import com.kwizz.service.ParticipantService;
import com.kwizz.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JoinController {

    private final ParticipantService participantService;
    private final SessionService sessionService;

    public JoinController(ParticipantService participantService, SessionService sessionService) {
        this.participantService = participantService;
        this.sessionService = sessionService;
    }

    // The "code" query param lets a scanned QR (which encodes
    // /join?code=XXXXXX) pre-fill the field instead of the participant
    // having to retype it.
    @GetMapping("/join")
    public String joinForm(@RequestParam(required = false) String code, Model model) {
        model.addAttribute("prefilledCode", code);
        return "join";
    }

    @PostMapping("/join")
    public String join(@RequestParam String code,
                       @RequestParam String nickname,
                       HttpSession session,
                       Model model) {
        try {
            Participant participant = participantService.joinSession(code, nickname);
            // This is the participant's entire "login" - one token in their
            // HttpSession, checked by playScreen() below on every visit.
            session.setAttribute("participantToken", participant.getSessionToken());
            return "redirect:/play/" + participant.getSession().getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("prefilledCode", code);
            return "join";
        }
    }

    @GetMapping("/play/{sessionId}")
    public String playScreen(@PathVariable Long sessionId, HttpSession session, Model model) {
        String token = (String) session.getAttribute("participantToken");
        if (token == null) {
            return "redirect:/join"; // never joined - send them back to start
        }
        model.addAttribute("participant", participantService.getByToken(token));
        model.addAttribute("session", sessionService.getSession(sessionId));
        return "play";
    }
}