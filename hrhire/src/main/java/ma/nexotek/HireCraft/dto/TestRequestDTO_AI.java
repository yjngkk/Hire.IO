package ma.nexotek.HireCraft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRequestDTO_AI {

     @NotBlank(message = "Test name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Subject is required")
    private String subject; 
    
    @NotBlank(message = "Domain is required") 
    private String domain; 

    private String theme; 
    @NotBlank(message = "Difficulty is required")
    private String difficulty; 
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "At least 1 question required")
    private Integer numberOfQuestions;
    
    @NotNull(message = "Duration is required")
    private Integer totalDuration; 
    
    @NotBlank(message = "AI provider is required")
    private String aiProvider; // "mistral" ou "groq"
}
    
