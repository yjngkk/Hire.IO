package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.ExerciseRequestDTO;
import ma.nexotek.HireCraft.dto.ExerciseResponseDTO;
import ma.nexotek.HireCraft.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExerciseMapper {

    @Autowired
    private QuestionMapper questionMapper;

    public Exercise toEntity(ExerciseRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Exercise exercise = new Exercise();
        exercise.setTitle(dto.getTitle());
        exercise.setType(dto.getType());
        exercise.setDomain(dto.getDomain());
        exercise.setTheme(dto.getTheme());
        exercise.setDifficulty(dto.getDifficulty());
        exercise.setDuration(dto.getDuration());
        exercise.setTotalPoints(dto.getTotalPoints());

        return exercise;
    }

    public void updateEntity(Exercise exercise, ExerciseRequestDTO dto) {
        if (dto == null || exercise == null) {
            return;
        }

        exercise.setTitle(dto.getTitle());
        exercise.setType(dto.getType());
        exercise.setDomain(dto.getDomain());
        exercise.setTheme(dto.getTheme());
        exercise.setDifficulty(dto.getDifficulty());
        exercise.setDuration(dto.getDuration());
        exercise.setTotalPoints(dto.getTotalPoints());
    }

    public ExerciseResponseDTO toResponseDTO(Exercise exercise) {
        if (exercise == null) {
            return null;
        }

        return ExerciseResponseDTO.builder()
                .id(exercise.getId())
                .title(exercise.getTitle())
                .type(exercise.getType())
                .domain(exercise.getDomain())
                .theme(exercise.getTheme())
                .difficulty(exercise.getDifficulty())
                .duration(exercise.getDuration())
                .totalPoints(exercise.getTotalPoints())
                .questions(questionMapper.toResponseDTOList(exercise.getQuestions()))
                .questionCount(exercise.getQuestions() != null ? exercise.getQuestions().size() : 0)
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }

    public List<ExerciseResponseDTO> toResponseDTOList(List<Exercise> exercises) {
        if (exercises == null) {
            return null;
        }

        return exercises.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}