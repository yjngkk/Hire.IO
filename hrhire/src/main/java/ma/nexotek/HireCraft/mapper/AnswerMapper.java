package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.AnswerRequestDTO;
import ma.nexotek.HireCraft.dto.AnswerResponseDTO;
import ma.nexotek.HireCraft.model.Answer;
import ma.nexotek.HireCraft.model.Question;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnswerMapper {

    public Answer toEntity(AnswerRequestDTO dto, Question question) {
        if (dto == null) {
            return null;
        }

        Answer answer = new Answer();
        answer.setAnswerText(dto.getAnswerText());
        answer.setAnswerIndex(dto.getAnswerIndex());
        answer.setQuestion(question);

        return answer;
    }

    public AnswerResponseDTO toResponseDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .answerText(answer.getAnswerText())
                .answerIndex(answer.getAnswerIndex())
                .build();
    }

    public List<Answer> toEntityList(List<AnswerRequestDTO> dtos, Question question) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(dto -> toEntity(dto, question))
                .collect(Collectors.toList());
    }

    public List<AnswerResponseDTO> toResponseDTOList(List<Answer> answers) {
        if (answers == null) {
            return null;
        }

        return answers.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
