package ma.nexotek.HireCraft.dto.Test;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
@Data
public class SubmitTestRequest {
    @NotNull
    private String accessToken;

    @NotNull
    @NotEmpty
    private List<QuestionAnswerDto> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerDto {
        @NotNull
        private Long questionId;

        @NotNull
        private Integer selectedAnswer;
    }
}
