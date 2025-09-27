package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_scoring_results", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"candidat_id", "form_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVScoringResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relations avec vos entités existantes
    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;
    
    @Column(name = "form_id", nullable = false)
    private Long formId;
    
    // Relations JPA (optionnelles pour affichage)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private Candidat candidat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    private Form form;
    
    // Scores IA
    @Column(name = "final_score", nullable = false)
    private Double finalScore;
    
    @Column(name = "skills_score")
    private Double skillsScore;
    
    @Column(name = "experience_score")
    private Double experienceScore;
    
    @Column(name = "semantic_score")
    private Double semanticScore;
    
    // Décision IA
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_decision", nullable = false)
    private DecisionType aiDecision;
    
    @Column(name = "confidence_level")
    private Double confidenceLevel;
    
    // Analyses textuelles (stockage JSON)
    @Lob
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Lob
    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills; // JSON: ["Java", "Spring Boot"]
    
    @Lob
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills; // JSON: ["Docker", "Kubernetes"]
    
    @Lob
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations; // JSON: ["Apprendre Docker", "Formation K8s"]
    
    // Métadonnées de scoring
    @Column(name = "scoring_date", nullable = false)
    private LocalDateTime scoringDate;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    // Statut avant/après
    @Column(name = "previous_status")
    private String previousStatus;
    
    @Column(name = "new_status")
    private String newStatus;
    
    // Méthodes utilitaires
    public boolean isAccepted() {
        return aiDecision == DecisionType.ACCEPT;
    }
    
    public boolean requiresInterview() {
        return aiDecision == DecisionType.INTERVIEW;
    }
    
    public boolean requiresReview() {
        return aiDecision == DecisionType.REVIEW;
    }
    
    public boolean isRejected() {
        return aiDecision == DecisionType.REJECT;
    }
    
    public String getDecisionDescription() {
        return aiDecision.getDescription();
    }
}
