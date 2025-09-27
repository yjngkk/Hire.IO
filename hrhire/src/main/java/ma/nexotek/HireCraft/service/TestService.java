package ma.nexotek.HireCraft.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import ma.nexotek.HireCraft.dto.*;
import ma.nexotek.HireCraft.mapper.TestMapper;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Test;
import ma.nexotek.HireCraft.repository.ExerciseRepository;
import ma.nexotek.HireCraft.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestService {



    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private TestMapper testMapper;



    public TestResponseDTO saveTest(@Valid TestRequestDTO testRequestDTO) {
        Test test = testMapper.toEntity(testRequestDTO);
        if (testRequestDTO.getExerciseIds() != null && !testRequestDTO.getExerciseIds().isEmpty()) {
            List<Exercise> managedExercises = new ArrayList<>();

            for (Long exerciseId : testRequestDTO.getExerciseIds()) {
                Exercise managedExercise = exerciseRepository.findById(exerciseId)
                        .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));
                managedExercises.add(managedExercise);
            }
            test.setExercises(managedExercises);
        }
        Test savedTest = testRepository.save(test);
        return testMapper.toResponseDTO(savedTest);
    }

    public List<TestResponseDTO> getAllTests() {
        List<Test> tests = testRepository.findAllWithExercises();
        tests.forEach(test -> {
            int exerciseCount = test.getExercises() != null ? test.getExercises().size() : 0;
        });

        return testMapper.toResponseDTOList(tests);
    }

    public Optional<TestResponseDTO> getTestById(Long id) {
        Optional<Test> testOptional = testRepository.findByIdWithExercises(id);
        return testOptional.map(testMapper::toResponseDTO);
    }

    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }

    public List<TestResponseDTO> searchTestsByName(String name) {
        List<Test> tests = testRepository.findByNameContainingIgnoreCase(name);
        return testMapper.toResponseDTOList(tests);
    }

    public TestResponseDTO updateTest(Long id, @Valid TestRequestDTO testRequestDTO) {
        Test existingTest = testRepository.findByIdWithExercises(id)
                .orElseThrow(() -> new RuntimeException("Test not found: " + id));
        testMapper.updateEntityFromDTO(existingTest, testRequestDTO);
        if (testRequestDTO.getExerciseIds() != null && !testRequestDTO.getExerciseIds().isEmpty()) {
            List<Exercise> managedExercises = new ArrayList<>();
            for (Long exerciseId : testRequestDTO.getExerciseIds()) {
                Exercise managedExercise = exerciseRepository.findById(exerciseId)
                        .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));
                managedExercises.add(managedExercise);
            }
            existingTest.setExercises(managedExercises);
        } else {
            existingTest.setExercises(new ArrayList<>());
        }
        Test updatedTest = testRepository.save(existingTest);
        return testMapper.toResponseDTO(updatedTest);
    }

    /**
     * Ajouter un exercice à un test
     */
    @Transactional
    public TestResponseDTO addExerciseToTest(Long testId, Long exerciseId) {
        // Récupérer le test
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with id: " + testId));

        // Récupérer l'exercice avec ses questions (EAGER fetch ou initialisation)
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

        // Vérifier si l'exercice n'est pas déjà dans le test
        if (test.getExercises().stream().anyMatch(e -> e.getId().equals(exerciseId))) {
            throw new RuntimeException("Exercise already exists in test");
        }

        // Ajouter l'exercice au test
        test.getExercises().add(exercise);

        // Mettre à jour les points totaux et la durée
        test.setTotalPoints(test.getTotalPoints() + exercise.getTotalPoints());
        test.setTotalDuration(test.getTotalDuration() + exercise.getDuration());

        // Sauvegarder
        Test savedTest = testRepository.save(test);

        // Forcer le chargement des questions pour chaque exercice
        savedTest.getExercises().forEach(ex -> {
            if (ex.getQuestions() != null) {
                ex.getQuestions().size(); // Force initialization
                ex.getQuestions().forEach(q -> {
                    if (q.getAnswers() != null) {
                        q.getAnswers().size(); // Force initialization
                    }
                });
            }
        });

        // Convertir et retourner
        return convertToResponseDTO(savedTest);
    }

    /**
     * Supprimer un exercice d'un test
     */
    @Transactional
    public TestResponseDTO removeExerciseFromTest(Long testId, Long exerciseId) {
        // Récupérer le test
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with id: " + testId));

        // Trouver l'exercice dans le test
        Exercise exerciseToRemove = test.getExercises().stream()
                .filter(e -> e.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exercise not found in test"));

        // Supprimer l'exercice du test
        test.getExercises().remove(exerciseToRemove);

        // Mettre à jour les points totaux et la durée
        test.setTotalPoints(Math.max(0, test.getTotalPoints() - exerciseToRemove.getTotalPoints()));
        test.setTotalDuration(Math.max(0, test.getTotalDuration() - exerciseToRemove.getDuration()));

        // Sauvegarder
        Test savedTest = testRepository.save(test);

        // Forcer le chargement des questions pour chaque exercice restant
        savedTest.getExercises().forEach(ex -> {
            if (ex.getQuestions() != null) {
                ex.getQuestions().size(); // Force initialization
                ex.getQuestions().forEach(q -> {
                    if (q.getAnswers() != null) {
                        q.getAnswers().size(); // Force initialization
                    }
                });
            }
        });

        // Convertir et retourner
        return convertToResponseDTO(savedTest);
    }
    @Transactional
    public List<ExerciseResponseDTO> getAvailableExercises(Long testId) {
        // Récupérer le test
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with id: " + testId));

        // Récupérer les IDs des exercices déjà dans le test
        List<Long> exerciseIdsInTest = test.getExercises().stream()
                .map(Exercise::getId)
                .collect(Collectors.toList());

        // Récupérer tous les exercices qui ne sont pas dans le test
        List<Exercise> availableExercises;
        if (exerciseIdsInTest.isEmpty()) {
            availableExercises = exerciseRepository.findAll();
        } else {
            availableExercises = exerciseRepository.findByIdNotIn(exerciseIdsInTest);
        }

        // Convertir en DTO
        return availableExercises.stream()
                .map(this::convertExerciseToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Méthode helper pour convertir Exercise en ExerciseResponseDTO
     * AVEC les questions et réponses
     */
    private ExerciseResponseDTO convertExerciseToResponseDTO(Exercise exercise) {
        // Convertir les questions avec leurs réponses
        List<QuestionResponseDTO> questionDTOs = null;
        if (exercise.getQuestions() != null) {
            questionDTOs = exercise.getQuestions().stream()
                    .map(question -> {
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
                    })
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
                .questions(questionDTOs) // Inclure les questions
                .questionCount(exercise.getQuestions() != null ? exercise.getQuestions().size() : 0)
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }


    /**
 * Convert Test entity to TestResponseDTO using existing mapper
 * Method to add in TestService class
 */
public TestResponseDTO convertToResponseDTO(Test test) {
    return testMapper.toResponseDTO(test);
}
   
}
