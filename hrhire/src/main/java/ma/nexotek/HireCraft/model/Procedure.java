package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String responsible;

    @Column(nullable = false)
    private String deadlineFormat;

    @Column(name = "due_date")
    private LocalDateTime dueDate;
    @Column(name = "due_hour")
    private Integer dueHour; // 0-23

    @Column(name = "due_minute")
    private Integer dueMinute; // 0-59

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Add status tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcedureStatus status = ProcedureStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ProcedureTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    // Constructor to create from template
    public Procedure(ProcedureTemplate template, Candidat candidat) {
        this.title = template.getTitle();
        this.responsible = template.getResponsible();
        this.deadlineFormat = template.getDeadline();
        this.description = template.getDescription();
        this.orderIndex = template.getOrderIndex();
        this.template = template;
        this.candidat = candidat;
        this.completed = false;
        this.status = ProcedureStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.completed == null) {
            this.completed = false;
        }
        if (this.status == null) {
            this.status = ProcedureStatus.PENDING;
        }
        // Calculate initial due date
        calculateAndSetDueDate();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
        // Update status based on current state
        updateStatus();
    }

    // Calculate due date from deadline format
    public void calculateAndSetDueDate() {
        if (this.deadlineFormat != null && this.createdDate != null) {
            this.dueDate = parseDeadlineToDate(this.createdDate, this.deadlineFormat);
        }
    }

    // Update status based on current date and completion
    public void updateStatus() {
        if (this.completed) {
            this.status = ProcedureStatus.COMPLETED;
        } else if (this.dueDate != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(this.dueDate)) {
                this.status = ProcedureStatus.OVERDUE;
            } else if (now.toLocalDate().equals(this.dueDate.toLocalDate())) {
                this.status = ProcedureStatus.DUE_TODAY;
            } else {
                this.status = ProcedureStatus.PENDING;
            }
        }
    }

    // Check if procedure is overdue
    public boolean isOverdue() {
        return !completed && dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    // Check if procedure is due today
    public boolean isDueToday() {
        return !completed && dueDate != null &&
                LocalDateTime.now().toLocalDate().equals(dueDate.toLocalDate());
    }

    // Get days until due (negative if overdue)
    public long getDaysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), dueDate.toLocalDate());
    }

    private LocalDateTime parseDeadlineToDate(LocalDateTime baseDate, String deadline) {
        try {
            deadline = deadline.trim().toUpperCase();

            if (deadline.startsWith("J+")) {
                String daysStr = deadline.substring(2);
                int days = Integer.parseInt(daysStr);
                return baseDate.plusDays(days);
            } else if (deadline.startsWith("J-")) {
                String daysStr = deadline.substring(2);
                int days = Integer.parseInt(daysStr);
                return baseDate.minusDays(days);
            } else if (deadline.equals("J")) {
                return baseDate;
            } else {
                return parseOtherDeadlineFormats(baseDate, deadline);
            }
        } catch (NumberFormatException e) {
            return baseDate.plusDays(1); // Default fallback
        }
    }

    private LocalDateTime parseOtherDeadlineFormats(LocalDateTime baseDate, String deadline) {
        deadline = deadline.trim().toUpperCase();

        if (deadline.contains("JOUR")) {
            String[] parts = deadline.split("\\s+");
            if (parts.length >= 1) {
                try {
                    int days = Integer.parseInt(parts[0]);
                    return baseDate.plusDays(days);
                } catch (NumberFormatException e) {
                    return baseDate.plusDays(1);
                }
            }
        } else if (deadline.contains("SEMAINE")) {
            String[] parts = deadline.split("\\s+");
            if (parts.length >= 1) {
                try {
                    int weeks = Integer.parseInt(parts[0]);
                    return baseDate.plusWeeks(weeks);
                } catch (NumberFormatException e) {
                    return baseDate.plusWeeks(1);
                }
            }
        }

        return baseDate.plusDays(1); // Default fallback
    }
}

