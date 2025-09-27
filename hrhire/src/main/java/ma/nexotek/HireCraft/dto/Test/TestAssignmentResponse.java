package ma.nexotek.HireCraft.dto.Test;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestAssignmentResponse {
    private Long assignmentId;
    private String candidatName;
    private String candidatEmail;
    private String testTitle;
    private String status;
    private String accessToken;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private String testUrl;
}
