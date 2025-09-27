package ma.nexotek.HireCraft.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureTemplateRequestDTO {
    private String title;
    private String responsible;
    private String deadline;
    private String description;
    private Integer orderIndex;
    private Boolean isActive;
}
