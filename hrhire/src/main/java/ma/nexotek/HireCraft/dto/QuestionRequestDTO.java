package ma.nexotek.HireCraft.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @NotNull(message = "Correct answer index is required")
    @Min(value = 0, message = "Correct answer index must be at least 0")
    private Integer correctAnswer;

    @Valid
    @NotEmpty(message = "At least one answer is required")
    private List<AnswerRequestDTO> answers;
}