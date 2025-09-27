package ma.nexotek.HireCraft.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingPlanDTO {
    private Long id;
    private String employeeName;
    private String notes;
    private String employeeEmail;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private Integer progress;
    private String manager;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    private Long candidatId;
}
