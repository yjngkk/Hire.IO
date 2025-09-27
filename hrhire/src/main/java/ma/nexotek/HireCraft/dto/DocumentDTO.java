package ma.nexotek.HireCraft.dto;

import lombok.Data;
import ma.nexotek.HireCraft.model.Document;

import java.time.LocalDateTime;
@Data
public class DocumentDTO {
    private Long id;
    private String name;
    private String filePath;
    private String type;
    private Long size;
    private LocalDateTime uploadDate;

    public DocumentDTO(Long id, String name, String filePath, String type, Long size, LocalDateTime uploadDate) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
    }

    // Constructeur depuis Document (sans le contenu)
    public DocumentDTO(Document document) {
        this.id = document.getId();
        this.name = document.getName();
        this.filePath = document.getFilePath();
        this.type = document.getType();
        this.size = document.getSize();
        this.uploadDate = document.getUploadDate();
    }
}
