package ma.nexotek.HireCraft.controller.Meeting;


import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.MeetingDto;
import ma.nexotek.HireCraft.service.EmailService;
import ma.nexotek.HireCraft.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class MeetingController {
    @Autowired
    private MeetingService meetingService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> scheduleMeeting(@RequestBody MeetingDto meetingDto) {
        try {
            log.info("Scheduling meeting for candidate: {}", meetingDto.getCandidateEmail());
            MeetingDto scheduled = meetingService.scheduleMeeting(meetingDto);
            log.info("Meeting scheduled successfully with ID: {}", scheduled.getId());
            return new ResponseEntity<>(scheduled, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error scheduling meeting: ", e);
            return new ResponseEntity<>(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/meetings"
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<?> cancelMeeting(@PathVariable String meetingId) {
        try {
            log.info("Cancelling meeting with ID: {}", meetingId);
            meetingService.cancelMeeting(meetingId);
            log.info("Meeting cancelled successfully: {}", meetingId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling meeting: ", e);
            return new ResponseEntity<>(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/meetings/" + meetingId
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<?> updateMeeting(@PathVariable String meetingId, @RequestBody MeetingDto meetingDto) {
        try {
            log.info("Updating meeting with ID: {}", meetingId);
            meetingDto.setId(Long.parseLong(meetingId));
            MeetingDto updated = meetingService.updateMeeting(meetingDto);
            log.info("Meeting updated successfully: {}", meetingId);
            emailService.sendDynamicMeetingEmail(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating meeting: ", e);
            return new ResponseEntity<>(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/meetings/" + meetingId
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getMeeting(@PathVariable String meetingId) {
        try {
            log.info("Fetching meeting with ID: {}", meetingId);
            MeetingDto meeting = meetingService.getMeeting(meetingId);
            log.info("Meeting fetched successfully: {}", meetingId);
            return ResponseEntity.ok(meeting);
        } catch (Exception e) {
            log.error("Error fetching meeting: ", e);
            return new ResponseEntity<>(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/meetings/" + meetingId
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllMeetings() {
        try {
            java.util.List<MeetingDto> meetings = meetingService.getAllMeetings();
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("Error fetching all meetings: ", e);
            return new ResponseEntity<>(
                Map.of(
                    "timestamp", java.time.LocalDateTime.now(),
                    "status", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/meetings/all"
                ),
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}