package ma.nexotek.HireCraft.mapper;

import ma.nexotek.HireCraft.dto.ZoomDto;
import ma.nexotek.HireCraft.model.ZoomMeeting;

import java.time.LocalDateTime;

public class ZoomMapper {
    public static ZoomDto toDto(ZoomMeeting meeting) {
        ZoomDto dto = new ZoomDto();
        dto.setMeetingId(meeting.getZoomId());
        dto.setTopic(meeting.getTopic());
        dto.setStartTime(meeting.getMeetingDate() != null ? meeting.getMeetingDate().toString() : null);
        dto.setJoinUrl(meeting.getJoinUrl());
        return dto;
    }

    public static ZoomMeeting toEntity(ZoomDto dto) {
        ZoomMeeting meeting = new ZoomMeeting();
        meeting.setZoomId(dto.getMeetingId());
        meeting.setTopic(dto.getTopic());
        meeting.setJoinUrl(dto.getJoinUrl());
        if (dto.getStartTime() != null) {
            meeting.setMeetingDate(LocalDateTime.parse(dto.getStartTime()));
        }
        return meeting;
    }
}
