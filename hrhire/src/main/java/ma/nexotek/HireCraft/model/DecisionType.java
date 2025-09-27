package ma.nexotek.HireCraft.model;

public enum DecisionType {
    ACCEPT("Candidat accepté - Profil excellent pour le poste"),
    INTERVIEW("Entretien recommandé - Candidat prometteur"),
    REVIEW("Révision manuelle requise - Profil à évaluer"),
    REJECT("Candidat rejeté - Profil inadéquat pour ce poste");
    
    private final String description;
    
    DecisionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Méthode pour convertir score en décision
    public static DecisionType fromScore(double score) {
        if (score >= 85) return ACCEPT;
        if (score >= 70) return INTERVIEW;
        if (score >= 50) return REVIEW;
        return REJECT;
    }
    
    // Méthode pour nouveau statut candidat
    public String toProcessStatus() {
        return switch (this) {
            case ACCEPT -> "ACCEPTED_AI";
            case INTERVIEW -> "INTERVIEW_SCHEDULED";
            case REVIEW -> "PENDING_REVIEW";
            case REJECT -> "REJECTED_AI";
        };
    }
}