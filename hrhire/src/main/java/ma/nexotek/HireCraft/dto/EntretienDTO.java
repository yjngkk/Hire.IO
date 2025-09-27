package ma.nexotek.HireCraft.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntretienDTO {
    private Long id;
    private CandidatDTO candidat;
    private LocalDateTime dateHeure;
    private String duree;
    private String type;
    private String interviewer;
    private String statut;
    private String notes;
    private String meetLink;
    private String organizerEmail;
    private String interviewerEmail;
    private Integer pauseDuration;
    private LocalDateTime pauseStartTime;
    
    // Champs Zoom
    private String zoomMeetingId;
    private String zoomMeetingUuid;
    private String zoomJoinUrl;
    private String zoomStartUrl;
    private String zoomPassword;
    private String zoomHostId;
}



