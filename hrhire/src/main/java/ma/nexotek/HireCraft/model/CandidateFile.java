package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateFile {

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

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "accepted_formats")
    private String acceptedFormats = ".pdf,.doc,.docx,.jpg,.jpeg,.png";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status = FileStatus.MISSING;

    @Column(name = "file_path")
    private String filePath; // Path to the uploaded file

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private CandidateFileTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @Column(name = "document_id")
    private Long documentId;


    public enum FileStatus {
        MISSING("missing"),
        PENDING("pending"),
        COMPLETED("completed"),
        REJECTED("rejected");

        private final String value;

        FileStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructor to create from template
    public CandidateFile(CandidateFileTemplate template, Candidat candidat) {
        this.name = template.getName();
        this.description = template.getDescription();
        this.orderIndex = template.getOrderIndex();
        this.isRequired = template.getIsRequired();
        this.documentType = template.getDocumentType();
        this.acceptedFormats = template.getAcceptedFormats();
        this.template = template;
        this.candidat = candidat;
        this.status = FileStatus.MISSING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = FileStatus.MISSING;
        }
        if (this.isRequired == null) {
            this.isRequired = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public void markAsUploaded(String filePath, String originalFilename, Long fileSize, String mimeType) {
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = FileStatus.COMPLETED;
        this.uploadedDate = LocalDateTime.now();
    }

    public void markAsPending() {
        this.status = FileStatus.PENDING;
    }

}
