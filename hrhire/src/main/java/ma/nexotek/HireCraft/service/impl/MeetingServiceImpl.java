package ma.nexotek.HireCraft.service.impl;

import ma.nexotek.HireCraft.dto.MeetingDto;
import ma.nexotek.HireCraft.mapper.MeetingMapper;
import ma.nexotek.HireCraft.model.Meeting;
import ma.nexotek.HireCraft.repository.MeetingRepository;
import ma.nexotek.HireCraft.service.EmailService;
import ma.nexotek.HireCraft.service.GoogleCalendarService;
import ma.nexotek.HireCraft.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;

@Service
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EmailService emailService;

    @Override
    public MeetingDto scheduleMeeting(MeetingDto meetingDto) {
        Meeting meeting = meetingMapper.toEntity(meetingDto);
        meeting.setStatus("scheduled"); // Définir le statut initial
        String meetLink = null;

        try {
            String position = (meeting.getPosition() != null && !meeting.getPosition().isEmpty()) ? meeting.getPosition() : "Technique";
            String meetSubject = "Entretien - " + position;
            String meetDescription = "Entretien technique pour le poste de " + position;
            meetLink = googleCalendarService.createMeetEvent(
                    meetSubject,
                    meetDescription,
                    meeting.getDateTime(),
                    meeting.getDateTime().plusHours(1),
                    meeting.getOrganizerEmail(),
                    meeting.getCandidateEmail(),
                    meeting.getInterviewerEmail()
            );

            if (meetLink == null) {
                throw new RuntimeException("Failed to get Google Meet link");
            }

            meeting.setMeetingLink(meetLink);
            meeting = meetingRepository.save(meeting);

            emailService.sendInterviewInvitation(
                    position,
                    meeting.getDateTime(),
                    meeting.getInterviewerEmail().split("@")[0], // Nom de l'intervieweur
                    meeting.getInterviewerEmail(),
                    meeting.getCandidateEmail().split("@")[0], // Nom du candidat
                    meeting.getCandidateEmail(),
                    meetLink,
                    meeting.getOrganizerEmail()
            );

            return meetingMapper.toDto(meeting);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create Google Meet event", e);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Failed to send email notifications", e);
        }
    }

    @Override
    public void cancelMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        meeting.setStatus("cancelled");
        meetingRepository.save(meeting);
    }

    @Override
    public MeetingDto updateMeeting(MeetingDto meetingDto) {
        Long id = meetingDto.getId();
        Meeting existing = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingDto.getId()));
        existing.setDateTime(meetingDto.getDateTime());
        existing.setOrganizerEmail(meetingDto.getOrganizerEmail());
        existing.setCandidateEmail(meetingDto.getCandidateEmail());
        existing.setInterviewerEmail(meetingDto.getInterviewerEmail());
        existing.setMeetingLink(meetingDto.getMeetingLink());
        existing.setStatus(meetingDto.getStatus() != null ? meetingDto.getStatus() : existing.getStatus());
        Meeting updated = meetingRepository.save(existing);
        return meetingMapper.toDto(updated);
    }

    @Override
    public MeetingDto getMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        return meetingMapper.toDto(meeting);
    }

    @Override
    public java.util.List<MeetingDto> getAllMeetings() {
        return meetingRepository.findAll().stream()
                .map(meetingMapper::toDto)
                .collect(Collectors.toList());
    }

    // Nouvelles méthodes pour la gestion des statuts
    public MeetingDto startMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        meeting.setStatus("in_progress");
        Meeting updated = meetingRepository.save(meeting);
        return meetingMapper.toDto(updated);
    }

    public MeetingDto pauseMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        meeting.setStatus("paused");
        Meeting updated = meetingRepository.save(meeting);
        return meetingMapper.toDto(updated);
    }

    public MeetingDto resumeMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        meeting.setStatus("in_progress");
        Meeting updated = meetingRepository.save(meeting);
        return meetingMapper.toDto(updated);
    }

    public MeetingDto completeMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findById(Long.parseLong(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));
        meeting.setStatus("completed");
        Meeting updated = meetingRepository.save(meeting);
        return meetingMapper.toDto(updated);
    }

    public java.util.List<MeetingDto> getMeetingsByStatus(String status) {
        return meetingRepository.findByStatus(status).stream()
                .map(meetingMapper::toDto)
                .collect(Collectors.toList());
    }
}