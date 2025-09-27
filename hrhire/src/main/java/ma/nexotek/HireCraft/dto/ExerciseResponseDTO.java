package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResponseDTO {

    private Long id;
    private String title;
    private String type;
    private String domain;
    private String theme;
    private String difficulty;
    private Integer duration;
    private Integer totalPoints;
    private List<QuestionResponseDTO> questions;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

