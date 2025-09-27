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
public class ExerciseRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Domain is required")
    private String domain;

    @NotBlank(message = "Theme is required")
    private String theme;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotNull(message = "Total points is required")
    @Min(value = 1, message = "Total points must be at least 1")
    private Integer totalPoints;

    @Valid
    private List<QuestionRequestDTO> questions;
}