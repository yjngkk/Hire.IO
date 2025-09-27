package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.TestRequestDTO;
import ma.nexotek.HireCraft.dto.TestResponseDTO;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Test;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestMapper {

    public Test toEntity(TestRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Test test = new Test();
        test.setName(dto.getName());
        test.setDescription(dto.getDescription());
        test.setDifficulty(dto.getDifficulty());
        test.setTotalDuration(dto.getTotalDuration());
        test.setTotalPoints(dto.getTotalPoints());
        test.setStatus(dto.getStatus());
        test.setCategorie(dto.getCategorie());
        return test;
    }


    public TestResponseDTO toResponseDTO(Test test) {
        if (test == null) {
            return null;
        }

        TestResponseDTO dto = new TestResponseDTO();
        dto.setId(test.getId());
        dto.setName(test.getName());
        dto.setDescription(test.getDescription());
        dto.setDifficulty(test.getDifficulty());
        dto.setTotalDuration(test.getTotalDuration());
        dto.setTotalPoints(test.getTotalPoints());
        dto.setStatus(test.getStatus());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setUpdatedAt(test.getUpdatedAt());

        // Map exercises
        if (test.getExercises() != null) {
            List<TestResponseDTO.ExerciseResponseDTO> exerciseDTOs = test.getExercises().stream()
                    .map(this::toExerciseResponseDTO)
                    .collect(Collectors.toList());
            dto.setExercises(exerciseDTOs);
        }

        return dto;
    }

    public List<TestResponseDTO> toResponseDTOList(List<Test> tests) {
        if (tests == null) {
            return null;
        }

        return tests.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    public void updateEntityFromDTO(Test test, TestRequestDTO dto) {
        if (test == null || dto == null) {
            return;
        }

        test.setName(dto.getName());
        test.setDescription(dto.getDescription());
        test.setDifficulty(dto.getDifficulty());
        test.setTotalDuration(dto.getTotalDuration());
        test.setTotalPoints(dto.getTotalPoints());
        test.setStatus(dto.getStatus());


    }


    private TestResponseDTO.ExerciseResponseDTO toExerciseResponseDTO(Exercise exercise) {
        if (exercise == null) {
            return null;
        }

        return new TestResponseDTO.ExerciseResponseDTO(
                exercise.getId(),
                exercise.getTitle(),
                exercise.getType(),
                exercise.getDifficulty(),
                exercise.getDuration(),
                exercise.getTotalPoints()
        );
        
    }

    
}
