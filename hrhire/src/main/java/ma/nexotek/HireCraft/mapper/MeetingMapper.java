package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.MeetingDto;
import ma.nexotek.HireCraft.model.Meeting;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {
    public Meeting toEntity(MeetingDto dto) {
        if (dto == null) return null;
        return Meeting.builder()
                .id(dto.getId())
                .organizerEmail(dto.getOrganizerEmail())
                .interviewerEmail(dto.getInterviewerEmail())
                .candidateEmail(dto.getCandidateEmail())
                .dateTime(dto.getDateTime())
                .meetingLink(dto.getMeetingLink())
                .position(dto.getPosition())
                .build();
    }

    public MeetingDto toDto(Meeting entity) {
        if (entity == null) return null;
        return MeetingDto.builder()
                .id(entity.getId())
                .organizerEmail(entity.getOrganizerEmail())
                .interviewerEmail(entity.getInterviewerEmail())
                .candidateEmail(entity.getCandidateEmail())
                .dateTime(entity.getDateTime())
                .meetingLink(entity.getMeetingLink())
                .position(entity.getPosition())
                .build();
    }
} 