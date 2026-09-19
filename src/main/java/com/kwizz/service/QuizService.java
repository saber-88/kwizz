package com.kwizz.service;

import com.kwizz.entity.Host;
import com.kwizz.entity.Question;
import com.kwizz.entity.Quiz;
import com.kwizz.repository.HostRepository;
import com.kwizz.repository.QuestionRepository;
import com.kwizz.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * All quiz/question business logic lives here rather than in the
 * controller. Controllers should stay thin: parse the request, call a
 * service method, pick a view/redirect - nothing more.
 */
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final HostRepository hostRepository;
    private final QuestionRepository questionRepository;

    public QuizService(QuizRepository quizRepository, HostRepository hostRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.hostRepository = hostRepository;
        this.questionRepository = questionRepository;
    }

    public List<Quiz> getQuizzesForHost(Long hostId) {
        return quizRepository.findByHostId(hostId);
    }

    public Quiz createQuiz(String title, String description, Long hostId) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new IllegalArgumentException("No host with id " + hostId));
        Quiz quiz = new Quiz(title, description, host);
        return quizRepository.save(quiz);
    }

    // @Transactional matters here: quiz.getQuestions() is a LAZY collection
    // (see the OneToMany on Quiz). Without an open transaction, accessing
    // it from a Thymeleaf template after this method returns throws
    // LazyInitializationException. Keeping the transaction open for the
    // whole method call means the collection is already loaded by the time
    // the controller hands the Quiz to the view.
    @Transactional(readOnly = true)
    public Quiz getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("No quiz with id " + quizId));
        quiz.getQuestions().size(); // force the lazy collection to load
        return quiz;
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("No quiz with id " + quizId));
        question.setQuiz(quiz);
        quiz.getQuestions().add(question);
        quizRepository.save(quiz); // cascade = ALL on Quiz.questions saves the new Question too
        return question;
    }

    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId); // cascade + orphanRemoval on Quiz.questions deletes its questions too
    }

    public void deleteQuestion(Long quizId, Long questionId){
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("No question with id " + questionId));

        if (!question.getQuiz().getId().equals(quizId)){
            throw new IllegalArgumentException("Question " + questionId + " does not belong to quiz " + quizId);
        }
        questionRepository.deleteById(questionId);
    }

}
