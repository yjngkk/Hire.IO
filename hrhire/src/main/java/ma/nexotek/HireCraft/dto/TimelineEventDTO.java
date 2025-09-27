package ma.nexotek.HireCraft.dto;

import ma.nexotek.HireCraft.enums.EventStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEventDTO {
    private Long id;
    private String eventType;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private EventStatus status;
    private String statusDisplay;
    private String performedBy;
    private String iconType;
    private String color;
    private String formattedDate;
    private String formattedTime;
}
