package ma.nexotek.HireCraft.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidat_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_assignment_id", nullable = false)
    private TestAssignment testAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer selectedAnswer;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer pointsEarned;

    @Column(nullable = false)
    private Integer pointsPossible;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime answeredAt;

}
