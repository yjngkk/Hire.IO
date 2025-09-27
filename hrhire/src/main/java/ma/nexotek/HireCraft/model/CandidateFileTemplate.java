package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_file_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateFileTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "accepted_formats")
    private String acceptedFormats = ".pdf,.doc,.docx,.jpg,.jpeg,.png";

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Constructor for initialization
    public CandidateFileTemplate(String name, String description, Integer orderIndex,
                                 Boolean isRequired, String documentType) {
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
        this.isRequired = isRequired;
        this.documentType = documentType;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isRequired == null) {
            this.isRequired = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}