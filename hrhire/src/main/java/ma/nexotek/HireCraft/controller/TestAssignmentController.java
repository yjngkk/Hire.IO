package ma.nexotek.HireCraft.controller;
import jakarta.validation.Valid;
import ma.nexotek.HireCraft.dto.*;
import ma.nexotek.HireCraft.dto.Test.*;
import ma.nexotek.HireCraft.repository.TestAssignmentRepository;
import ma.nexotek.HireCraft.service.TestAssignmentService;
import ma.nexotek.HireCraft.model.TestAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-assignments")
public class TestAssignmentController {
    @Autowired
    private TestAssignmentService testAssignmentService;

    @Autowired
    private TestAssignmentRepository testAssignmentRepository;


    @GetMapping
    public ResponseEntity<List<TestAssignmentDto>> getAllTestAssignments() {
        try {
            List<TestAssignmentDto> assignments = testAssignmentService.getAllTestAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/send")
    public ResponseEntity<?> sendTest(@RequestBody SendTestRequest request) {
        try {
            TestAssignmentResponse response = testAssignmentService.sendTestToCandidat(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur", "details", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<TestResultResponse> submitTest(
            @Valid @RequestBody SubmitTestRequest request) {
        try {
            TestResultResponse response = testAssignmentService.submitTest(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/results/{assignmentId}")
    public ResponseEntity<TestResultResponse> getTestResults(
            @PathVariable Long assignmentId) {
        try {
            TestResultResponse response = testAssignmentService.getTestResults(assignmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<TestAssignment>> getCandidatAssignments(
            @PathVariable Long candidatId) {
        List<TestAssignment> assignments = testAssignmentService.getAssignmentsByCandidat(candidatId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/candidat/{candidatId}/results")
    public ResponseEntity<List<TestResultResponse>> getCandidatResults(
            @PathVariable Long candidatId) {
        try {
            List<TestAssignment> assignments = testAssignmentService.getAssignmentsByCandidat(candidatId);
            List<TestResultResponse> results = assignments.stream()
                    .filter(assignment -> "COMPLETED".equals(assignment.getStatus()))
                    .map(assignment -> testAssignmentService.getTestResults(assignment.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TestAssignment>> getPendingAssignments() {
        List<TestAssignment> assignments = testAssignmentService.getPendingAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/token/{accessToken}")
    public ResponseEntity<TestAssignmentDto> getAssignmentByToken(
            @PathVariable String accessToken) {
        try {
            TestAssignmentDto assignment = testAssignmentService.getAssignmentByToken(accessToken);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'assignment: " + e.getMessage());
            e.printStackTrace(); //
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/start/{accessToken}")
    public ResponseEntity<?> startTest(@PathVariable String accessToken) {
        try {
            testAssignmentService.markTestAsStarted(accessToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/send-random")
    public ResponseEntity<?> sendRandomTest(@RequestBody SendRandomTestRequest request) {
        try {
            TestAssignmentResponse response = testAssignmentService.sendRandomTestToCandidat(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur", "details", e.getMessage()));
        }
    }

    // Add this method to your TestAssignmentController.java

    @GetMapping("/weekly-stats")
    public ResponseEntity<Map<String, Integer>> getWeeklyStats() {
        try {
            Map<String, Integer> stats = testAssignmentService.getWeeklyTestStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
