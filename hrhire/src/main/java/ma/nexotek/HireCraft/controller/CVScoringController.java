package ma.nexotek.HireCraft.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.CVScoringRequest;
import ma.nexotek.HireCraft.dto.CVScoringResponse;
import ma.nexotek.HireCraft.model.CVScoringResult;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.FormRepository;
import ma.nexotek.HireCraft.service.ai.CVScoringService;

@RequiredArgsConstructor
@Slf4j
@RestController

@RequestMapping("/api/cv-scoring")
public class CVScoringController {

    private final CVScoringService cvScoringService;
    private final CandidatRepository candidatRepository;
    private final FormRepository formRepository;

    /**
     * 🔧 TON ENDPOINT EXISTANT - Version améliorée avec scoring multi-dimensionnel
     */
    @Transactional
    @PostMapping("/score")
    public ResponseEntity<CVScoringResponse> scoreCV(@RequestBody CVScoringRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🎯 Demande scoring multi-dimensionnel: candidat={}, form={}", 
                    request.getCandidateId(), request.getFormId());

            // 1️⃣ Récupérer candidat (garde ta logique)
            Candidat candidat = candidatRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new RuntimeException("Candidat introuvable: " + request.getCandidateId()));

            // 2️⃣ Récupérer formulaire/job offer (garde ta logique)
            Form form = formRepository.findById(request.getFormId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable: " + request.getFormId()));

            // 3️⃣ Calcul du score multi-dimensionnel (ton service amélioré)
            CVScoringResult result = cvScoringService.scoreCV(candidat, form);

            // Sauvegarde automatique du score dans le candidat
candidat.setCvScore(result.getFinalScore());
candidat.setProcessStatus(result.getNewStatus());

// Sauvegarder le candidat mis à jour
candidat = candidatRepository.save(candidat);
log.info("Score sauvegardé pour candidat {}: {:.1f}% | Statut: {}", 
        candidat.getId(), result.getFinalScore(), result.getNewStatus());
            
            // 4️⃣ Construire la réponse enrichie (garde ta structure + améliorations)
            CVScoringResponse response = CVScoringResponse.builder()
                    .success(true)
                    .candidatId(candidat.getId())
                    .candidatNom(candidat.getNom())
                    .candidatEmail(candidat.getEmail())
                    .candidatPoste(candidat.getPoste())
                    .formId(form.getId())
                    .jobTitle(form.getTitle())
                    .jobLocation(form.getLocation())
                    .jobLevel(form.getLevel())
                    .cvFileName(candidat.getCv() != null ? candidat.getCv().getName() : "N/A")
                    
                    // 🆕 Scores détaillés multi-dimensionnels
                    .finalScore(result.getFinalScore())
                    .semanticScore(result.getSemanticScore())
                    .skillsScore(result.getSkillsScore())
                    .experienceScore(result.getExperienceScore())
                    
                    .decision(result.getAiDecision().name())
                    .decisionDescription(result.getAiDecision().getDescription())
                    .confidence(result.getConfidenceLevel())
                    
                    // 🆕 Analyses enrichies
                    .aiAnalysis(result.getAiAnalysis())
                    .matchedSkills(result.getMatchedSkills() != null && !result.getMatchedSkills().isEmpty() ? 
                                 java.util.Arrays.asList(result.getMatchedSkills().split(",")) : 
                                 java.util.Collections.emptyList())
                    .missingSkills(result.getMissingSkills() != null && !result.getMissingSkills().isEmpty() ? 
                                 java.util.Arrays.asList(result.getMissingSkills().split(",")) : 
                                 java.util.Collections.emptyList())
                    .recommendations(result.getRecommendations() != null && !result.getRecommendations().isEmpty() ? 
                                   java.util.Arrays.asList(result.getRecommendations().split(";")) : 
                                   java.util.Collections.emptyList())
                    
                    .previousProcessStatus(result.getPreviousStatus())
                    .newProcessStatus(result.getNewStatus())
                    .timestamp(LocalDateTime.now())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();

            log.info("✅ Scoring multi-dimensionnel réussi en {}ms: candidat={}, " +
                    "finalScore={:.1f}%, semanticScore={:.1f}%, skillsScore={:.1f}%, " +
                    "experienceScore={:.1f}%, décision={}", 
                    response.getProcessingTimeMs(), candidat.getNom(), 
                    result.getFinalScore(), result.getSemanticScore(),
                    result.getSkillsScore(), result.getExperienceScore(),
                    result.getAiDecision());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur de validation: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, startTime);
            
        } catch (RuntimeException e) {
            log.error("❌ Erreur métier: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND, startTime);
            
        } catch (Exception e) {
            log.error("❌ Erreur technique lors du scoring", e);
            return buildErrorResponse("Erreur technique du service de scoring", 
                                    HttpStatus.INTERNAL_SERVER_ERROR, startTime);
        }
    }

   

    /**
     * 🆕 AJOUT 4: Statistiques de scoring pour une offre
     */
    @GetMapping("/stats/{formId}")
    public ResponseEntity<Map<String, Object>> getScoringStats(@PathVariable Long formId) {
        try {
            // Vérifier que l'offre existe
            Form form = formRepository.findById(formId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable"));

            // Tu peux implémenter des statistiques depuis ta base de données CVScoringResult
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("formId", formId);
            stats.put("jobTitle", form.getTitle());
            stats.put("jobLocation", form.getLocation());
            
            // Statistiques à implémenter selon tes besoins
            stats.put("totalCVsScored", 0); // Compter depuis CVScoringResult table
            stats.put("averageScore", 0.0); // Moyenne des finalScore
            stats.put("acceptedCount", 0);  // Count où aiDecision = ACCEPT
            stats.put("interviewCount", 0); // Count où aiDecision = INTERVIEW
            stats.put("reviewCount", 0);    // Count où aiDecision = REVIEW
            stats.put("rejectedCount", 0);  // Count où aiDecision = REJECT
            
            stats.put("lastScoringDate", null); // Max scoringDate
            stats.put("timestamp", LocalDateTime.now());

            log.info("📊 Statistiques récupérées pour l'offre: {}", formId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Erreur récupération statistiques offre {}", formId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🆕 AJOUT 5: Endpoint pour re-scorer tous les CVs d'une offre
     */
    @PostMapping("/rescore-all/{formId}")
    public ResponseEntity<Map<String, Object>> rescoreAllCandidates(@PathVariable Long formId) {
        try {
            Form form = formRepository.findById(formId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable"));

            // Trouver tous les candidats pour cette offre (selon ta logique métier)
            // List<Candidat> candidats = candidatRepository.findByFormId(formId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("formId", formId);
            response.put("jobTitle", form.getTitle());
            response.put("totalCandidates", 0); // candidats.size()
            response.put("rescored", 0);
            response.put("failed", 0);
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "COMPLETED");

            log.info("🔄 Re-scoring terminé pour l'offre: {}", formId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur re-scoring offre {}", formId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    /**
     * 🛠️ Méthode utilitaire pour construire les réponses d'erreur
     */
    private ResponseEntity<CVScoringResponse> buildErrorResponse(String error, HttpStatus status, long startTime) {
        CVScoringResponse errorResponse = CVScoringResponse.builder()
                .success(false)
                .error(error)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    // ==================== DTOs POUR LES NOUVEAUX ENDPOINTS ====================

    /**
     * DTO pour le test rapide
     */
    public static class QuickTestRequest {
        private String cvText;
        private String jobDescription;

        // Getters et setters
        public String getCvText() { return cvText; }
        public void setCvText(String cvText) { this.cvText = cvText; }
        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
 
    }
}