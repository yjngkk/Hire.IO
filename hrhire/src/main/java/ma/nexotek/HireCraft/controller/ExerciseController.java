package ma.nexotek.HireCraft.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import ma.nexotek.HireCraft.dto.ExerciseRequestDTO;
import ma.nexotek.HireCraft.dto.ExerciseResponseDTO;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Test;
import ma.nexotek.HireCraft.repository.ExerciseRepository;
import ma.nexotek.HireCraft.repository.TestRepository;
import ma.nexotek.HireCraft.service.ExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;



    @PostMapping
    public ResponseEntity<?> createExercise(@Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        try {
            ExerciseResponseDTO createdExercise = exerciseService.createExercise(exerciseRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExercise);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to create exercise: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllExercises() {
        try {
            List<ExerciseResponseDTO> exercises = exerciseService.getAllExercises();
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve exercises: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExerciseById(@PathVariable Long id) {
        try {
            ExerciseResponseDTO exercise = exerciseService.getExerciseById(id);
            if (exercise != null) {
                return ResponseEntity.ok(exercise);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve exercise: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExercise(@PathVariable Long id,
                                            @Valid @RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        try {
            ExerciseResponseDTO updatedExercise = exerciseService.updateExercise(id, exerciseRequestDTO);
            return ResponseEntity.ok(updatedExercise);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
        try {
            boolean deleted = exerciseService.deleteExercise(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "message", "Exercise deleted successfully",
                        "id", id
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Exercise not found"));

        } catch (RuntimeException e) {
            // Handle business logic errors (like foreign key constraints)
            if (e.getMessage().contains("used in") && e.getMessage().contains("test")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", e.getMessage(),
                                "code", "EXERCISE_IN_USE"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to delete exercise: " + e.getMessage(),
                            "code", "INTERNAL_ERROR"
                    ));
        }
    }


    @GetMapping("/test/{testId}")
    public ResponseEntity<?> getExercisesByTestId(@PathVariable Long testId) {
        try {
            List<ExerciseResponseDTO> exercises = exerciseService.getExercisesByTestId(testId);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve exercises for test: " + e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredExercises(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        try {
            List<ExerciseResponseDTO> exercises = exerciseService.getFilteredExercises(
                    domain, theme, difficulty, type, search);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to filter exercises: " + e.getMessage()));
        }
    }
}