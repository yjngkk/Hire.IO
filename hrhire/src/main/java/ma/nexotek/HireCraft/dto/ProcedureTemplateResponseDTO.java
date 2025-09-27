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
public class ProcedureTemplateResponseDTO {
    private Long id;
    private String title;
    private String responsible;
    private String deadline;
    private String description;
    private Integer orderIndex;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
