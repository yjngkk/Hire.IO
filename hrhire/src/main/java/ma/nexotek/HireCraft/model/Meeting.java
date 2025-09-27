package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "meeting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String organizerEmail;
    private String interviewerEmail;
    private String position;
    private String candidateEmail;
    private LocalDateTime dateTime;
    private String meetingLink;
    private String status; // "scheduled", "in_progress", "completed", "cancelled", "paused"

} 