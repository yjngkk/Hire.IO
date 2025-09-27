package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "progress")
    private Integer progress; // 0-100

    @Column(name = "manager")
    private String manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OnboardingStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-One relationship with Candidat
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", unique = true)
    private Candidat candidat;

    // Additional fields for onboarding management
    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (progress == null) {
            progress = 0;
        }
        if (status == null) {
            status = OnboardingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for onboarding status
    public enum OnboardingStatus {
        PENDING("pending"),
        IN_PROGRESS("in-progress"),
        COMPLETED("completed"),
        ON_HOLD("on-hold"),
        CANCELLED("cancelled");

        private final String value;

        OnboardingStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}