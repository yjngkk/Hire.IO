package ma.nexotek.HireCraft.dto.Test;

import lombok.Data;

@Data
public class SendRandomTestRequest {
    private Long candidatId;
    private Long offreId;
    private String message;

    // Constructeurs
    public SendRandomTestRequest() {}

    public SendRandomTestRequest(Long candidatId, Long offreId, String message) {
        this.candidatId = candidatId;
        this.offreId = offreId;
        this.message = message;
    }
}
