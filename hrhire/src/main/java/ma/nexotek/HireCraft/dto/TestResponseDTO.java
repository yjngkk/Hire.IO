package ma.nexotek.HireCraft.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TestResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private Integer totalDuration;
    private Integer totalPoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExerciseResponseDTO> exercises;

    // Constructors
    public TestResponseDTO() {}

    public TestResponseDTO(Long id, String name, String description, String difficulty,
                           Integer totalDuration, Integer totalPoints, String status,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           List<ExerciseResponseDTO> exercises) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.totalDuration = totalDuration;
        this.totalPoints = totalPoints;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.exercises = exercises;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ExerciseResponseDTO> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseResponseDTO> exercises) {
        this.exercises = exercises;
    }

    // Nested DTO for Exercise responses
    public static class ExerciseResponseDTO {
        private Long id;
        private String title;
        private String type;
        private String difficulty;
        private Integer duration;
        private Integer points;

        // Constructors
        public ExerciseResponseDTO() {}

        public ExerciseResponseDTO(Long id, String title, String type, String difficulty,
                                   Integer duration, Integer points) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.difficulty = difficulty;
            this.duration = duration;
            this.points = points;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }
    }
}