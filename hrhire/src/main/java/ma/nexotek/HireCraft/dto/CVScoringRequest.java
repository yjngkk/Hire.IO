package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CVScoringRequest {
    private Long candidateId;
    private Long formId;
    private Long documentId; // ← ID du document CV spécifique
    // Ou laisser null pour auto-détection du CV du candidat
}