package ma.nexotek.HireCraft.controller.Onboarding;

import lombok.RequiredArgsConstructor;
import ma.nexotek.HireCraft.dto.CandidateFileDTO;
import ma.nexotek.HireCraft.dto.OnboardingPlanDTO;
import ma.nexotek.HireCraft.service.Onboarding.OnboardingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/onboarding-plans")
@RequiredArgsConstructor
public class OnboardingPlanController {

    @Autowired
    private OnboardingPlanService onboardingPlanService;

    @GetMapping
    public ResponseEntity<List<OnboardingPlanDTO>> getAllOnboardingPlans() {
        List<OnboardingPlanDTO> plans = onboardingPlanService.getAllOnboardingPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/active")
    public ResponseEntity<List<OnboardingPlanDTO>> getActiveOnboardingPlans() {
        List<OnboardingPlanDTO> plans = onboardingPlanService.getActiveOnboardingPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnboardingPlanDTO> getOnboardingPlanById(@PathVariable Long id) {
        return onboardingPlanService.getOnboardingPlanById(id)
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<OnboardingPlanDTO> getOnboardingPlanByCandidatId(@PathVariable Long candidatId) {
        return onboardingPlanService.getOnboardingPlanByCandidatId(candidatId)
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OnboardingPlanDTO>> getOnboardingPlansByStatus(@PathVariable String status) {
        try {
            List<OnboardingPlanDTO> plans = onboardingPlanService.getOnboardingPlansByStatus(status);
            return ResponseEntity.ok(plans);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<OnboardingPlanDTO> createOnboardingPlan(@RequestBody OnboardingPlanDTO dto) {
        try {
            OnboardingPlanDTO created = onboardingPlanService.createOnboardingPlan(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OnboardingPlanDTO> updateOnboardingPlan(
            @PathVariable Long id,
            @RequestBody OnboardingPlanDTO dto) {
        try {
            OnboardingPlanDTO updated = onboardingPlanService.updateOnboardingPlan(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Add this endpoint to your OnboardingPlanController.java

    @PostMapping("/{id}/generate-files")
    public ResponseEntity<List<CandidateFileDTO>> generateCandidateFiles(@PathVariable Long id) {
        try {
            // Get the onboarding plan to find the candidat ID
            Optional<OnboardingPlanDTO> plan = onboardingPlanService.getOnboardingPlanById(id);
            if (!plan.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            List<CandidateFileDTO> createdFiles = onboardingPlanService.generateCandidateFilesFromTemplates(plan.get().getCandidatId());
            return ResponseEntity.ok(createdFiles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Alternative endpoint to generate files directly by candidat ID
    @PostMapping("/candidat/{candidatId}/generate-files")
    public ResponseEntity<List<CandidateFileDTO>> generateCandidateFilesByCandidatId(@PathVariable Long candidatId) {
        try {
            List<CandidateFileDTO> createdFiles = onboardingPlanService.generateCandidateFilesFromTemplates(candidatId);
            return ResponseEntity.ok(createdFiles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OnboardingPlanDTO> patchOnboardingPlan(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        System.out.println("PATCH request received for ID: " + id + " with updates: " + updates);

        try {
            // Handle progress update specifically
            if (updates.containsKey("progress")) {
                Object progressObj = updates.get("progress");
                Integer progress;

                if (progressObj instanceof Integer) {
                    progress = (Integer) progressObj;
                } else if (progressObj instanceof Double) {
                    progress = ((Double) progressObj).intValue();
                } else {
                    progress = Integer.valueOf(progressObj.toString());
                }

                if (progress < 0 || progress > 100) {
                    return ResponseEntity.badRequest().build();
                }

                OnboardingPlanDTO updated = onboardingPlanService.updateProgress(id, progress);
                System.out.println("Progress updated successfully: " + updated);
                return ResponseEntity.ok(updated);
            }

            // Handle other partial updates if needed
            return ResponseEntity.badRequest().build();

        } catch (NumberFormatException e) {
            System.err.println("Invalid progress format: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("Error updating progress: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOnboardingPlan(@PathVariable Long id) {
        try {
            onboardingPlanService.deleteOnboardingPlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Add an OPTIONS handler for preflight requests
    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}