package ma.nexotek.HireCraft.controller;

import lombok.RequiredArgsConstructor;
import ma.nexotek.HireCraft.dto.AnswerResponseDTO;
import ma.nexotek.HireCraft.dto.QuestionResponseDTO;
import ma.nexotek.HireCraft.dto.TestRequestDTO;
import ma.nexotek.HireCraft.dto.TestRequestDTO_AI;
import ma.nexotek.HireCraft.dto.ExerciseResponseDTO;
import ma.nexotek.HireCraft.dto.TestResponseDTO;
import ma.nexotek.HireCraft.model.Answer;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Question;
import ma.nexotek.HireCraft.model.Test;
import ma.nexotek.HireCraft.repository.AnswerRepository;
import ma.nexotek.HireCraft.repository.QuestionRepository;
import ma.nexotek.HireCraft.service.AITestGenerationService;
import ma.nexotek.HireCraft.service.TestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private AITestGenerationService aiTestGenerationService;
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired 
    private AnswerRepository answerRepository;
 
    private static final Logger log = LoggerFactory.getLogger(TestController.class);


    @GetMapping
    public ResponseEntity<List<TestResponseDTO>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponseDTO> getTestById(@PathVariable Long id) {
        return testService.getTestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TestResponseDTO> createTest(@Valid @RequestBody TestRequestDTO testRequestDTO) {
        TestResponseDTO savedTest = testService.saveTest(testRequestDTO);
        return ResponseEntity.ok(savedTest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestResponseDTO> updateTest(@PathVariable Long id, @Valid @RequestBody TestRequestDTO testRequestDTO) {
        return testService.getTestById(id)
                .map(existingTest -> {
                    TestResponseDTO updatedTest = testService.updateTest(id, testRequestDTO);
                    return ResponseEntity.ok(updatedTest);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        if (testService.getTestById(id).isPresent()) {
            testService.deleteTest(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<TestResponseDTO>> searchTests(@RequestParam String name) {
        return ResponseEntity.ok(testService.searchTestsByName(name));
    }
    @PostMapping("/{testId}/exercises/{exerciseId}")
    public ResponseEntity<?> addExerciseToTest(
            @PathVariable Long testId,
            @PathVariable Long exerciseId) {
        try {
            TestResponseDTO updatedTest = testService.addExerciseToTest(testId, exerciseId);
            return ResponseEntity.ok(updatedTest);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding exercise to test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to add exercise to test"));
        }
    }

    /**
     * Supprimer un exercice d'un test
     */
    @DeleteMapping("/{testId}/exercises/{exerciseId}")
    public ResponseEntity<?> removeExerciseFromTest(
            @PathVariable Long testId,
            @PathVariable Long exerciseId) {
        try {
            TestResponseDTO updatedTest = testService.removeExerciseFromTest(testId, exerciseId);
            return ResponseEntity.ok(updatedTest);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing exercise from test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to remove exercise from test"));
        }
    }

    /**
     * Obtenir les exercices disponibles qui ne sont pas encore dans le test
     */
    @GetMapping("/{testId}/available-exercises")
    public ResponseEntity<?> getAvailableExercises(@PathVariable Long testId) {
        try {
            List<ExerciseResponseDTO> availableExercises = testService.getAvailableExercises(testId);
            return ResponseEntity.ok(availableExercises);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting available exercises: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get available exercises"));
        }
    }


/**
 génération de QCM via message libre ( zeema interface chatbot)
 */
@PostMapping("/generate-from-message")
public ResponseEntity<ExerciseResponseDTO> generateQcmFromMessage(@RequestBody Map<String, String> request) {
    try {
        String userMessage = request.get("message");
        String aiProvider = request.getOrDefault("aiProvider", "mistral");
        
        log.info("Generating QCM from user message: {}", userMessage);
        
        // 1. Générer le test
        Test generatedTest = aiTestGenerationService.generateQcmFromUserMessage(userMessage, aiProvider);
        
        // 2. Récupérer le test avec toutes les questions/réponses
        Optional<Test> testWithDetails = aiTestGenerationService.getTestById(generatedTest.getId());
        if (testWithDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test test = testWithDetails.get();
        Exercise exercise = test.getExercises().get(0);
        
        // 3. Charger manuellement les questions et réponses
        List<Question> questions = questionRepository.findByExerciseIdOrderById(exercise.getId());
        exercise.setQuestions(questions);
        
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionIdOrderByAnswerIndex(question.getId());
            question.setAnswers(answers);
        }
        
        // 4. Convertir vers ExerciseResponseDTO complet
        ExerciseResponseDTO response = convertToFullExerciseDTO(exercise);
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        log.error("Error generating QCM from message: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}


private ExerciseResponseDTO convertToFullExerciseDTO(Exercise exercise) {
    List<QuestionResponseDTO> questionDTOs = null;
    
    if (exercise.getQuestions() != null) {
        questionDTOs = exercise.getQuestions().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }
    
    return ExerciseResponseDTO.builder()
            .id(exercise.getId())
            .title(exercise.getTitle())
            .type(exercise.getType())
            .domain(exercise.getDomain())
            .theme(exercise.getTheme())
            .difficulty(exercise.getDifficulty())
            .duration(exercise.getDuration())
            .totalPoints(exercise.getTotalPoints())
            .questions(questionDTOs)
            .questionCount(questionDTOs != null ? questionDTOs.size() : 0)
            .createdAt(exercise.getCreatedAt())
            .updatedAt(exercise.getUpdatedAt())
            .build();
}

private QuestionResponseDTO convertToQuestionDTO(Question question) {
    List<AnswerResponseDTO> answerDTOs = null;
    if (question.getAnswers() != null) {
        answerDTOs = question.getAnswers().stream()
                .map(answer -> AnswerResponseDTO.builder()
                        .id(answer.getId())
                        .answerText(answer.getAnswerText())
                        .answerIndex(answer.getAnswerIndex())
                        .build())
                .collect(Collectors.toList());
    }
    
    return QuestionResponseDTO.builder()
            .id(question.getId())
            .questionText(question.getQuestionText())
            .points(question.getPoints())
            .correctAnswer(question.getCorrectAnswer())
            .answers(answerDTOs)
            .build();
}


}