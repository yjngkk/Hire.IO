package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "entretien")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"cv"})
public class Entretien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    private LocalDateTime dateHeure;
    // Durée possible : "30min", "45min", "1h", "1h30min"
    private String duree;
    // Type possible : "meet" (en ligne), "presentiel"
    private String type;
    // Nom de l'interviewer
    private String interviewer="Nexotek";
    private String statut; // prévu, en_cours, terminé, etc.
    private String notes;
    private String meetLink;
    private String organizerEmail;
    private String interviewerEmail;
    private Integer pauseDuration; // Durée de pause en minutes
    private LocalDateTime pauseStartTime;
    
    // Champs Zoom
    private String zoomMeetingId;
    private String zoomMeetingUuid;
    private String zoomJoinUrl;
    private String zoomStartUrl;
    private String zoomPassword;
    private String zoomHostId;
} 