package ma.nexotek.HireCraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

public class TestRequestDTO {

    private String name;

    private String description;

    private String difficulty;

    private Integer totalDuration;

    private Integer totalPoints;

    private String status = "Brouillon";

    private List<Long> exerciseIds;
    private String categorie;

    // Constructors
    public TestRequestDTO() {}

    public TestRequestDTO(String name, String description, String difficulty,
                          Integer totalDuration, Integer totalPoints, String status,
                          List<Long> exerciseIds) {
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.totalDuration = totalDuration;
        this.totalPoints = totalPoints;
        this.status = status;
        this.exerciseIds = exerciseIds;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Long> getExerciseIds() {
        return exerciseIds;
    }

    public void setExerciseIds(List<Long> exerciseIds) {
        this.exerciseIds = exerciseIds;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
}