package ma.nexotek.HireCraft.dto.Test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendTestRequest {

    private Long candidatId;
    private Long testId;
    private String message;
}
