package ma.nexotek.HireCraft.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.nexotek.HireCraft.model.ProcedureStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureDTO {

    private Long id;
    private String title;
    private String responsible;

    private String deadline;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime dueDate;

    // Add status information
    private ProcedureStatus status;

    private Boolean completed;
    private String description;
    private Integer orderIndex;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Candidat information
    private Long candidatId;
    private String candidatNom;
    private String candidatEmail;
    private String candidatPoste;

    // Template information
    private Long templateId;
    private String templateTitle;

    // Calculated fields for UI
    private long daysUntilDue;
    private boolean isOverdue;
    private boolean isDueToday;

    // Helper methods for UI display
    public String getStatusDisplayName() {
        return switch (status) {
            case PENDING -> "En attente";
            case DUE_TODAY -> "Échéance aujourd'hui";
            case OVERDUE -> "En retard";
            case COMPLETED -> "Terminé";
        };
    }

    public String getStatusColor() {
        return switch (status) {
            case PENDING -> "blue";
            case DUE_TODAY -> "yellow";
            case OVERDUE -> "red";
            case COMPLETED -> "green";
        };
    }

    public String getDueDateFormatted() {
        if (dueDate == null) return "Non définie";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dueDate.format(formatter);
    }

    public String getRelativeTimeDescription() {
        if (dueDate == null) return "Non définie";

        LocalDateTime now = LocalDateTime.now();
        long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), dueDate.toLocalDate());

        if (daysUntil < 0) {
            return "En retard de " + Math.abs(daysUntil) + " jour(s)";
        } else if (daysUntil == 0) {
            return "Aujourd'hui";
        } else if (daysUntil == 1) {
            return "Demain";
        } else {
            return "Dans " + daysUntil + " jours";
        }
    }

    public boolean isUrgent() {
        return status == ProcedureStatus.OVERDUE || status == ProcedureStatus.DUE_TODAY;
    }
}