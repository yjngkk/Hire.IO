package ma.nexotek.HireCraft.controller;

import ma.nexotek.HireCraft.service.ZoomService;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.ZoomMeeting;
import ma.nexotek.HireCraft.dto.ZoomDto;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.ZoomMeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {
    @Autowired
    private ZoomService zoomService;
    @Autowired
    private CandidatRepository candidatRepository;
    @Autowired
    private ZoomMeetingRepository zoomMeetingRepository;

    public static class CreateMeetingRequest {
        private String topic;
        private String dateTime; // ISO string
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    }

    @PostMapping("/create-meeting")
    public ResponseEntity<?> createMeeting(@RequestBody CreateMeetingRequest request) {
        try {
            java.time.LocalDateTime meetingDate = java.time.LocalDateTime.parse(request.getDateTime());
            ZoomDto zoomDto = zoomService.createMeeting(request.getTopic(), "dummy@email.com", meetingDate);
            return ResponseEntity.ok(zoomDto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/send-link/{candidatId}")
    public ResponseEntity<?> createMeetingAndSendLink(@PathVariable Long candidatId, @RequestBody CreateMeetingRequest request) {
        return candidatRepository.findById(candidatId)
            .map(candidat -> {
                if (candidat.getEmail() == null || candidat.getEmail().isEmpty()) {
                    return ResponseEntity.status(400).body("Candidat email missing");
                }
                try {
                    java.time.LocalDateTime meetingDate = java.time.LocalDateTime.parse(request.getDateTime());
                    ZoomDto zoomDto = zoomService.createMeeting(request.getTopic(), candidat.getEmail(), meetingDate);
                    return ResponseEntity.ok(zoomDto);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Error: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.status(404).body("Candidat not found"));
    }

    @PostMapping("/create-and-send/{candidatId}")
    public ResponseEntity<?> createZoomAndSendEmail(@PathVariable Long candidatId, @RequestBody CreateMeetingRequest request) {
        return candidatRepository.findById(candidatId)
            .map(candidat -> {
                if (candidat.getEmail() == null || candidat.getEmail().isEmpty()) {
                    return ResponseEntity.status(400).body("Candidat email missing");
                }
                try {
                    java.time.LocalDateTime meetingDate = java.time.LocalDateTime.parse(request.getDateTime());
                    ZoomDto zoomDto = zoomService.createMeeting(request.getTopic(), candidat.getEmail(), meetingDate);
                    // Save meeting info
                    ZoomMeeting meeting = ZoomMeeting.builder()
                        .zoomId(zoomDto.getMeetingId())
                        .topic(zoomDto.getTopic())
                        .joinUrl(zoomDto.getJoinUrl())
                        .meetingDate(meetingDate)
                        .candidat(candidat)
                        .build();
                    zoomMeetingRepository.save(meeting);
                    return ResponseEntity.ok(zoomDto);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Error: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.status(404).body("Candidat not found"));
    }

    @GetMapping("/list-meetings")
    public ResponseEntity<String> listMeetings() {
        try {
            String accessToken = zoomService.getAccessToken();
            String url = "https://api.zoom.us/v2/users/me/meetings";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(java.util.List.of(org.springframework.http.MediaType.APPLICATION_JSON));
            org.springframework.http.HttpEntity<Void> request = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/local-meetings")
    public ResponseEntity<?> getAllLocalMeetings() {
        java.util.List<ZoomMeeting> meetings = zoomMeetingRepository.findAll();
        java.util.List<ZoomDto> dtos = meetings.stream().map(ma.nexotek.HireCraft.mapper.ZoomMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }
}
