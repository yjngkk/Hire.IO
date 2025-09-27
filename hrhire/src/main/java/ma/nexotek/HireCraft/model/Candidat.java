package ma.nexotek.HireCraft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import java.util.List;

@Entity
@Table(name = "candidat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String poste;
    private String notes;
    private String telephone;

    @Column(name = "bon_talent")
    private Boolean bonTalent = false;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "cv_id")
    private Document cv;

    // Changed to One-to-Many relationship with Procedures
    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Procedure> procedures;

    // Add the candidate files relationship
    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CandidateFile> candidateFiles;

    // Add the onboarding plan relationship
    @OneToOne(mappedBy = "candidat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OnboardingPlan onboardingPlan;

    @Column(nullable = true)
    private Double cvScore;

    @Column(nullable = true)
    private String processStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = true)
    private Form offre;


    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("eventDate DESC")
    private List<TimelineEvent> timelineEvents;

}