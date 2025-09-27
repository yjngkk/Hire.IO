package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CVScoringResponse {
    private boolean success;
    
    // Identifiants
    private Long candidatId;
    private String candidatNom;
    private String candidatEmail;
    private String candidatPoste; // Le poste souhaité du candidat
    
    private Long formId;
    private String jobTitle;      // Form.title
    private String jobLocation;   // Form.location
    private String jobLevel;      // Form.level
    
    private String cvFileName;    // candidat.cv.name
    
    // Scores IA
    private Double finalScore;
    private Double skillsScore;
    private Double experienceScore;
    private Double semanticScore;
    
    // Décision IA
    private String decision; // ACCEPT, INTERVIEW, REVIEW, REJECT
    private String decisionDescription;
    private Double confidence;
    
    // Analyses détaillées
    private String aiAnalysis;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> recommendations;
    
    // Matching détails
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String skillsMatchPercentage;
    
    // Métadonnées
    private String error;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
    
    // Statut du processus
    private String previousProcessStatus; // Ancien statut du candidat
    private String newProcessStatus;      // Nouveau statut après scoring
}