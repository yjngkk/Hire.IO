package ma.nexotek.HireCraft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureUpdateDTO {
    private Boolean completed;
    private String notes; // Optional notes when completing a procedure
}