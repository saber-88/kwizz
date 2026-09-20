package com.kwizz.controller;

import com.kwizz.entity.Question;
import com.kwizz.entity.Quiz;
import com.kwizz.repository.HostRepository;
import com.kwizz.service.QuizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;


    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }



    @GetMapping
    public String listQuizzes(Model model, Principal principal) {
        model.addAttribute("quizzes", quizService.getQuizzesForHost(quizService.getCurrentHostId(principal)));
        return "quiz/list";
    }

    @GetMapping("/new")
    public String newQuizForm() {
        return "quiz/form";
    }

    @PostMapping
    public String createQuiz(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             Principal principal) {
        Quiz quiz = quizService.createQuiz(title, description, quizService.getCurrentHostId(principal));
        return "redirect:/quizzes/" + quiz.getId();
    }

    @GetMapping("/{id}")
    public String quizDetail(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getQuiz(id));
        return "quiz/detail";
    }

    @PostMapping("/{id}/questions")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam String text,
                              @RequestParam String optionA,
                              @RequestParam String optionB,
                              @RequestParam String optionC,
                              @RequestParam String optionD,
                              @RequestParam int correctOption,
                              @RequestParam(defaultValue = "20") int timeLimitSeconds) {
        Question question = new Question();
        question.setText(text);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectOption(correctOption);
        question.setTimeLimitSeconds(timeLimitSeconds);

        quizService.addQuestion(id, question);
        return "redirect:/quizzes/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return "redirect:/quizzes";
    }

    @PostMapping("{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId){
        quizService.deleteQuestion(quizId,questionId);
        return "redirect:/quizzes/" + quizId;
    }
}
