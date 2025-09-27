package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDTO {

    private Long id;
    private String questionText;
    private Integer points;
    private Integer correctAnswer;
    private List<AnswerResponseDTO> answers;
}