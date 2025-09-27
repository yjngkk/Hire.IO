package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.QuestionRequestDTO;
import ma.nexotek.HireCraft.dto.QuestionResponseDTO;
import ma.nexotek.HireCraft.model.Question;
import ma.nexotek.HireCraft.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    @Autowired
    private AnswerMapper answerMapper;

    public Question toEntity(QuestionRequestDTO dto, Exercise exercise) {
        if (dto == null) {
            return null;
        }

        Question question = new Question();
        question.setQuestionText(dto.getQuestionText());
        question.setPoints(dto.getPoints());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExercise(exercise);

        return question;
    }

    public QuestionResponseDTO toResponseDTO(Question question) {
        if (question == null) {
            return null;
        }

        return QuestionResponseDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .points(question.getPoints())
                .correctAnswer(question.getCorrectAnswer())
                .answers(answerMapper.toResponseDTOList(question.getAnswers()))
                .build();
    }

    public List<Question> toEntityList(List<QuestionRequestDTO> dtos, Exercise exercise) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(dto -> toEntity(dto, exercise))
                .collect(Collectors.toList());
    }

    public List<QuestionResponseDTO> toResponseDTOList(List<Question> questions) {
        if (questions == null) {
            return null;
        }

        return questions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}