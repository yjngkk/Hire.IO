package ma.nexotek.HireCraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureResponseDTO {
    private Long id;
    private String title;
    private String responsible;
    private String deadline;
    private String description;
    private Integer orderIndex;
    private Boolean completed;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Template info
    private Long templateId;
    private String templateTitle;

    // Candidat info
    private Long candidatId;
    private String candidatName;
}