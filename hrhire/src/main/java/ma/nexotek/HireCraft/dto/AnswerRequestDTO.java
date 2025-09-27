package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequestDTO {

    @NotBlank(message = "Answer text is required")
    private String answerText;

    @NotNull(message = "Answer index is required")
    @Min(value = 0, message = "Answer index must be at least 0")
    private Integer answerIndex;
}

