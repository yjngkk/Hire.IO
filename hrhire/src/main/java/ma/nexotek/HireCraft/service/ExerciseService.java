package ma.nexotek.HireCraft.service;

import lombok.RequiredArgsConstructor;

import ma.nexotek.HireCraft.dto.ExerciseRequestDTO;
import ma.nexotek.HireCraft.dto.ExerciseResponseDTO;
import ma.nexotek.HireCraft.mapper.ExerciseMapper;
import ma.nexotek.HireCraft.mapper.QuestionMapper;
import ma.nexotek.HireCraft.mapper.AnswerMapper;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Question;
import ma.nexotek.HireCraft.model.Answer;
import ma.nexotek.HireCraft.model.Test;
import ma.nexotek.HireCraft.repository.ExerciseRepository;
import ma.nexotek.HireCraft.repository.QuestionRepository;
import ma.nexotek.HireCraft.repository.AnswerRepository;
import ma.nexotek.HireCraft.repository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ExerciseMapper exerciseMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final TestRepository testRepository;

    @Transactional
    public ExerciseResponseDTO createExercise(ExerciseRequestDTO exerciseRequestDTO) {
        // Convert DTO to Entity
        Exercise exercise = exerciseMapper.toEntity(exerciseRequestDTO);

        // Save exercise first to get ID
        exercise = exerciseRepository.save(exercise);

        // Process questions if they exist
        if (exerciseRequestDTO.getQuestions() != null && !exerciseRequestDTO.getQuestions().isEmpty()) {
            Exercise finalExercise = exercise;

            List<Question> questions = exerciseRequestDTO.getQuestions().stream().map(questionDTO -> {
                Question question = questionMapper.toEntity(questionDTO, finalExercise);
                question = questionRepository.save(question);

                // Process answers if they exist
                if (questionDTO.getAnswers() != null && !questionDTO.getAnswers().isEmpty()) {
                    Question finalQuestion = question;
                    List<Answer> answers = questionDTO.getAnswers().stream()
                            .map(answerDTO -> answerMapper.toEntity(answerDTO, finalQuestion))
                            .collect(Collectors.toList());

                    // Save all answers
                    answers = answerRepository.saveAll(answers);
                    question.setAnswers(answers);
                }

                return question;
            }).collect(Collectors.toList());

            exercise.setQuestions(questions);
        }

        // Convert back to DTO for response
        return exerciseMapper.toResponseDTO(exercise);
    }

    @Transactional
    public ExerciseResponseDTO updateExercise(Long id, ExerciseRequestDTO exerciseRequestDTO) {
        Exercise existingExercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));

        // Update basic exercise fields using mapper
        exerciseMapper.updateEntity(existingExercise, exerciseRequestDTO);

        // Handle questions update - let orphanRemoval handle the deletion
        if (exerciseRequestDTO.getQuestions() != null) {
            // Clear existing questions - orphanRemoval will automatically delete them
            existingExercise.getQuestions().clear();

            // Create new questions from DTO
            List<Question> newQuestions = exerciseRequestDTO.getQuestions().stream().map(questionDTO -> {
                Question question = questionMapper.toEntity(questionDTO, existingExercise);

                // Handle answers
                if (questionDTO.getAnswers() != null && !questionDTO.getAnswers().isEmpty()) {
                    List<Answer> answers = questionDTO.getAnswers().stream()
                            .map(answerDTO -> answerMapper.toEntity(answerDTO, question))
                            .collect(Collectors.toList());

                    // Use the helper method if you added it, or direct assignment
                    question.setAnswers(answers);
                }

                return question;
            }).collect(Collectors.toList());

            // Add new questions to the exercise
            for (Question question : newQuestions) {
                existingExercise.getQuestions().add(question);
            }
        }

        // Save the exercise - cascade will handle saving questions and answers
        Exercise savedExercise = exerciseRepository.save(existingExercise);
        return exerciseMapper.toResponseDTO(savedExercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> getAllExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();
        return exerciseMapper.toResponseDTOList(exercises);
    }

    @Transactional(readOnly = true)
    public ExerciseResponseDTO getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id).orElse(null);
        return exerciseMapper.toResponseDTO(exercise);
    }

    @jakarta.transaction.Transactional
    public boolean deleteExercise(Long id) {
        try {
            // First, check if exercise exists
            Exercise exercise = exerciseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));

            // Option 1: Remove exercise from all tests (recommended)
            // This removes the exercise from the many-to-many relationship without deleting the tests
            List<Test> testsUsingExercise = testRepository.findByExercisesContaining(exercise);
            for (Test test : testsUsingExercise) {
                test.getExercises().remove(exercise);
                testRepository.save(test);
            }

            // Now safe to delete the exercise
            exerciseRepository.delete(exercise);

            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete exercise: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> getExercisesByTestId(Long testId) {
        List<Exercise> exercises = exerciseRepository.findByTestId(testId);
        return exerciseMapper.toResponseDTOList(exercises);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> getFilteredExercises(String domain, String theme,
                                                          String difficulty, String type, String search) {
        List<Exercise> exercises = exerciseRepository.findAll();

        List<Exercise> filteredExercises = exercises.stream()
                .filter(exercise -> domain == null || exercise.getDomain().equalsIgnoreCase(domain))
                .filter(exercise -> theme == null || exercise.getTheme().equalsIgnoreCase(theme))
                .filter(exercise -> difficulty == null || exercise.getDifficulty().equalsIgnoreCase(difficulty))
                .filter(exercise -> type == null || exercise.getType().equalsIgnoreCase(type))
                .filter(exercise -> search == null ||
                        exercise.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                        exercise.getDomain().toLowerCase().contains(search.toLowerCase()) ||
                        exercise.getTheme().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        return exerciseMapper.toResponseDTOList(filteredExercises);
    }
}