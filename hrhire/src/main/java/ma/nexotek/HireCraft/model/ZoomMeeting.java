package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "zoom_meeting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoomMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String zoomId;
    private String topic;
    private String joinUrl;
    private LocalDateTime meetingDate;
    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}
