package ma.nexotek.HireCraft.controller;

import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.dto.JobOfferRequest;
import ma.nexotek.HireCraft.service.JobOfferGenerationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/job-offers")
public class JobOfferController {
    
    private static final Logger log = LoggerFactory.getLogger(JobOfferController.class);
    
    @Autowired
    private JobOfferGenerationService jobOfferGenerationService;
    
    
    
    /**
     * Generate a new job offer using AI
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateJobOffer(
        @Valid @RequestBody JobOfferRequest request,
            Authentication authentication) {
                
    log.info("Generating job offer for position: {}", request.getTitle());
    
    try {
        String generatedContent = jobOfferGenerationService.generateJobOffer(request); 
        log.info("Job offer generated successfully");
        return ResponseEntity.ok(generatedContent);
    } catch (Exception error) {
        log.error("Job offer generation failed", error);
        return ResponseEntity.badRequest().body("Erreur: " + error.getMessage());
    }
}
    
    /**
     * Generate with automatic fallback between AI models
     */
    @PostMapping("/generate-with-fallback")
   public ResponseEntity<String> generateJobOfferWithFallback(
           @Valid @RequestBody JobOfferRequest request,
           Authentication authentication) {
       
       log.info("Generating job offer with fallback for position: {}", request.getTitle());
       
       try {
           String generatedContent = jobOfferGenerationService.generateJobOfferWithFallback(request);
           log.info("Job offer generated successfully with fallback");
           return ResponseEntity.ok(generatedContent);
       } catch (Exception error) {
           log.error("All AI models failed for position: {}", request.getTitle(), error);
           return ResponseEntity.internalServerError().body("Tous les modèles AI sont indisponibles");
       }
   }
    
    /**
     * Get all job offers for current tenant
     */
    @GetMapping
    public ResponseEntity<List<Form>> getAllJobOffers(Authentication authentication) {
        List<Form> jobOffers = jobOfferGenerationService.getAllJobOffers();
        return ResponseEntity.ok(jobOffers);
    }
    
    /**
     * Get job offer by ID (tenant-scoped)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Form> getJobOfferById(@PathVariable Long id, Authentication authentication) {
        Optional<Form> jobOffer = jobOfferGenerationService.getJobOfferById(id);
        return jobOffer.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete job offer by ID (tenant-scoped)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id, Authentication authentication) {
        boolean deleted = jobOfferGenerationService.deleteJobOffer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Get available AI models
     */
    @GetMapping("/models")
    public ResponseEntity<String[]> getAvailableModels() {
        String[] models = {"mistral", "groq", "huggingface"};
        return ResponseEntity.ok(models);
    }
    
    /**
     * Health check for AI services
     */
    @GetMapping("/health")
public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Job Offer Generation Service is running");
}
    

}