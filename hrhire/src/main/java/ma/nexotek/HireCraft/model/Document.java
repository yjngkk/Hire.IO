package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "type")
    private String type;

    @Column(name = "size")
    private Long size;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content")
    private byte[] content;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;


    public Document(String name, String filePath, String type, Long size, byte[] content) {
        this.name = name;
        this.filePath = filePath;
        this.type = type;
        this.size = size;
        this.content = content;
        this.uploadDate = LocalDateTime.now();
    }
}
