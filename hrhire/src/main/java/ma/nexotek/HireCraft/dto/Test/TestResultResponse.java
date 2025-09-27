package ma.nexotek.HireCraft.dto.Test;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestResultResponse {
    private Long assignmentId;
    private String candidatName;
    private String testTitle;
    private Integer totalScore;
    private Integer totalPossibleScore;
    private Double percentage;
    private String status;
    private LocalDateTime completedAt;
    private List<QuestionResultDto> questionResults;

    @Data
    @Builder
    public static class QuestionResultDto {
        private Long questionId;
        private String questionText;
        private Integer selectedAnswer;
        private Integer correctAnswer;
        private Boolean isCorrect;
        private Integer pointsEarned;
        private Integer pointsPossible;
        private List<String> answerOptions;
        private String selectedAnswerText;
        private String correctAnswerText;
    }
}
