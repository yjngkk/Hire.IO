package ma.nexotek.HireCraft.mapper;

import ma.nexotek.HireCraft.dto.TimelineEventDTO;
import ma.nexotek.HireCraft.model.TimelineEvent;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimelineEventMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TimelineEventDTO toDTO(TimelineEvent event) {
        return TimelineEventDTO.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .status(event.getStatus())
                .iconType(event.getIconType())
                .statusDisplay(event.getStatus().getDisplayName())
                .formattedDate(event.getEventDate().format(DATE_FORMATTER))
                .formattedTime(event.getEventDate().format(TIME_FORMATTER))
                .build();
    }

    public List<TimelineEventDTO> toDTOList(List<TimelineEvent> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}