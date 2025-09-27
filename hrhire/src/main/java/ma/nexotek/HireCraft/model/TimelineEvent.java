package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import ma.nexotek.HireCraft.enums.EventStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "timeline_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(length = 50,nullable = true)
    private String iconType;


}
