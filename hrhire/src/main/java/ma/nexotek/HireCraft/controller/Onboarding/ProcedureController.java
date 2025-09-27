package ma.nexotek.HireCraft.controller.Onboarding;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.ProcedureDTO;
import ma.nexotek.HireCraft.service.Onboarding.OnboardingPlanService;
import ma.nexotek.HireCraft.service.Onboarding.ProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedures")
@RequiredArgsConstructor
@Slf4j
public class ProcedureController {

    @Autowired
    private ProcedureService procedureService;
    @Autowired
    private OnboardingPlanService onboardingPlanService;

    // Get all procedures
    @GetMapping
    public ResponseEntity<List<ProcedureDTO>> getAllProcedures() {
        try {
            log.info("Getting all procedures");
            List<ProcedureDTO> procedures = procedureService.getAllProcedures();
            log.info("Found {} procedures", procedures.size());
            return ResponseEntity.ok(procedures);
        } catch (Exception e) {
            log.error("Error getting all procedures", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get procedures by candidat ID
    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<ProcedureDTO>> getProceduresByCandidatId(@PathVariable Long candidatId) {
        try {
            List<ProcedureDTO> procedures = procedureService.getProceduresByCandidatId(candidatId);
            return ResponseEntity.ok(procedures);
        } catch (Exception e) {
            log.error("Error getting procedures for candidat {}", candidatId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get procedure by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProcedureDTO> getProcedureById(@PathVariable Long id) {
        try {
            return procedureService.getProcedureById(id)
                    .map(procedure -> ResponseEntity.ok(procedure))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting procedure {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Create new procedure
    @PostMapping
    public ResponseEntity<ProcedureDTO> createProcedure(@Valid @RequestBody ProcedureDTO procedureDTO) {
        try {
            ProcedureDTO createdProcedure = procedureService.createProcedure(procedureDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProcedure);
        } catch (RuntimeException e) {
            log.error("Error creating procedure", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating procedure", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Update existing procedure
    @PutMapping("/{id}")
    public ResponseEntity<ProcedureDTO> updateProcedure(
            @PathVariable Long id,
            @Valid @RequestBody ProcedureDTO procedureDTO) {
        try {
            return procedureService.updateProcedure(id, procedureDTO)
                    .map(updatedProcedure -> ResponseEntity.ok(updatedProcedure))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            log.error("Error updating procedure {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating procedure {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Generate procedures from templates for a specific candidate
    @PostMapping("/generate-from-templates/{candidatId}")
    public ResponseEntity<List<ProcedureDTO>> generateProceduresFromTemplates(@PathVariable Long candidatId) {
        try {
            List<ProcedureDTO> procedures = onboardingPlanService.generateProceduresFromTemplates(candidatId);
            return ResponseEntity.ok(procedures);
        } catch (RuntimeException e) {
            log.error("Error generating procedures from templates for candidat {}", candidatId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error generating procedures from templates for candidat {}", candidatId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ProcedureDTO> toggleProcedureCompletion(@PathVariable Long id) {
        try {
            return procedureService.toggleProcedureCompletion(id)
                    .map(updatedProcedure -> ResponseEntity.ok(updatedProcedure))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error toggling procedure completion {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete procedure
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcedure(@PathVariable Long id) {
        try {
            boolean deleted = procedureService.deleteProcedure(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting procedure {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get completion statistics
    @GetMapping("/stats")
    public ResponseEntity<ProcedureService.ProcedureStatsDTO> getCompletionStats() {
        try {
            ProcedureService.ProcedureStatsDTO stats = procedureService.getCompletionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting completion stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Reset all procedures to incomplete state
    @PostMapping("/reset")
    public ResponseEntity<List<ProcedureDTO>> resetAllProcedures() {
        try {
            List<ProcedureDTO> resetProcedures = procedureService.resetAllProcedures();
            return ResponseEntity.ok(resetProcedures);
        } catch (Exception e) {
            log.error("Error resetting all procedures", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reset-to-defaults")
    public ResponseEntity<List<ProcedureDTO>> resetToDefaultProcedures() {
        try {
            List<ProcedureDTO> defaultProcedures = procedureService.resetAllProcedures();
            return ResponseEntity.ok(defaultProcedures);
        } catch (Exception e) {
            log.error("Error resetting to default procedures", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/upcoming-events")
    public ResponseEntity<List<ProcedureService.UpcomingEventDTO>> getUpcomingEvents() {
        try {
            log.info("Getting upcoming events with default parameters");
            List<ProcedureService.UpcomingEventDTO> upcomingEvents = procedureService.getUpcomingEvents();
            log.info("Found {} upcoming events", upcomingEvents.size());
            return ResponseEntity.ok(upcomingEvents);
        } catch (Exception e) {
            log.error("Error getting upcoming events", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/upcoming-events/{daysAhead}/{limit}")
    public ResponseEntity<List<ProcedureService.UpcomingEventDTO>> getUpcomingEventsWithParams(
            @PathVariable("daysAhead") int daysAhead,
            @PathVariable("limit") int limit) {
        try {
            log.info("Getting upcoming events with daysAhead={}, limit={}", daysAhead, limit);

            // Validate parameters
            if (daysAhead < 1 || daysAhead > 365) {
                log.warn("Invalid daysAhead parameter: {}", daysAhead);
                return ResponseEntity.badRequest().build();
            }
            if (limit < 1 || limit > 100) {
                log.warn("Invalid limit parameter: {}", limit);
                return ResponseEntity.badRequest().build();
            }

            List<ProcedureService.UpcomingEventDTO> upcomingEvents = procedureService.getUpcomingEvents(daysAhead, limit);
            log.info("Found {} upcoming events for daysAhead={}, limit={}", upcomingEvents.size(), daysAhead, limit);
            return ResponseEntity.ok(upcomingEvents);
        } catch (Exception e) {
            log.error("Error getting upcoming events with daysAhead={}, limit={}", daysAhead, limit, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get completion statistics by candidat
    @GetMapping("/stats/candidat/{candidatId}")
    public ResponseEntity<ProcedureService.ProcedureStatsDTO> getCompletionStatsByCandidat(@PathVariable Long candidatId) {
        try {
            ProcedureService.ProcedureStatsDTO stats = procedureService.getCompletionStatsByCandidat(candidatId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting completion stats for candidat {}", candidatId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ADDED: Health check endpoint for debugging
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check called");
        return ResponseEntity.ok("ProcedureController is running");
    }
    @GetMapping("/upcoming-events/today")
    public ResponseEntity<List<ProcedureService.UpcomingEventDTO>> getTodayEvents() {
        try {
            log.info("Getting today's events (J+0 deadline)");
            List<ProcedureService.UpcomingEventDTO> todayEvents = procedureService.getTodayEvents();
            log.info("Found {} candidates with procedures due today", todayEvents.size());
            return ResponseEntity.ok(todayEvents);
        } catch (Exception e) {
            log.error("Error getting today's events", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}