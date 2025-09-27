package ma.nexotek.HireCraft.service;


import ma.nexotek.HireCraft.dto.MeetingDto;

public interface MeetingService {
    MeetingDto scheduleMeeting(MeetingDto meetingDto);
    void cancelMeeting(String meetingId);
    MeetingDto updateMeeting(MeetingDto meetingDto);
    MeetingDto getMeeting(String meetingId);
    java.util.List<MeetingDto> getAllMeetings();
} 