package ma.nexotek.HireCraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private String organizerEmail;
    private String interviewerEmail;
    private String position;
    private String candidateEmail;
    private LocalDateTime dateTime;
    private String meetingLink;
    private String status;
}