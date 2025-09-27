package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "procedure_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String responsible;

    @Column(nullable = false)
    private String deadline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex; // To maintain order of procedures

    @Column(name = "is_active")
    private Boolean isActive = true; // To enable/disable templates

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Constructor for initialization
    public ProcedureTemplate(String title, String responsible, String deadline, String description, Integer orderIndex) {
        this.title = title;
        this.responsible = responsible;
        this.deadline = deadline;
        this.description = description;
        this.orderIndex = orderIndex;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}