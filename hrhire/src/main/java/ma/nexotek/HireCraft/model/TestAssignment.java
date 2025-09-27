package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "test_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, unique = true)
    private String accessToken;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private Integer score;

    @Column
    private Integer totalPossibleScore;

    @Column
    private Double percentage;

    @OneToMany(mappedBy = "testAssignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CandidatAnswer> candidatAnswers = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;
}
