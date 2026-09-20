package com.kwizz.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kwizz.dto.QuestionMessage;
import com.kwizz.entity.Question;
import com.kwizz.entity.QuizSession;
import com.kwizz.service.SessionService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Controller
public class SessionController {

    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionController(SessionService sessionService, SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/quizzes/{quizId}/sessions")
    public String startSession(@PathVariable Long quizId){
        QuizSession session = sessionService.startSession(quizId);
        return "redirect:/sessions/" + session.getId();
    }

    @GetMapping("/sessions/{id}")
    public String dashboard(@PathVariable Long id, Model model){
        model.addAttribute("quizSession",sessionService.getSession(id));
        return "session/host-dashboard";
    }

    @PostMapping("/sessions/{id}/next")
    public String nextQuestion(@PathVariable Long id) {
        sessionService.nextQuestion(id);
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/sessions/{id}/stop")
    public String stopSession(@PathVariable Long id) {
        sessionService.stopSession(id);
        return "redirect:/sessions/" + id;
    }

    private void broadcastCurrentState(Long sessionId, QuizSession session) {
        String destination = "/topic/session/" + sessionId + "/question";

        if (session.getStatus() == QuizSession.Status.IN_PROGRESS) {
            Question q = session.getQuiz().getQuestions().get(session.getCurrentQuestionIndex());
            QuestionMessage message = new QuestionMessage(
                    q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                    q.getTimeLimitSeconds(),
                    session.getCurrentQuestionIndex() + 1,
                    session.getQuiz().getQuestions().size()
            );
            messagingTemplate.convertAndSend(destination, message);
        } else if (session.getStatus() == QuizSession.Status.FINISHED) {
            messagingTemplate.convertAndSend(destination, Map.of("finished", true));
        }
    }


    @GetMapping(value = "/sessions/{id}/qr.png", produces = "image/png")
    @ResponseBody
    public byte[] qrCode(@PathVariable Long id, HttpServletRequest request) throws WriterException, IOException {
        QuizSession session = sessionService.getSession(id);

        String joinUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/join")
                .replaceQuery("code=" + session.getJoinCode())
                .build()
                .toUriString();

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(joinUrl, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

}
