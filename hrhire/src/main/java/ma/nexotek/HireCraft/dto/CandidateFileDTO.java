package ma.nexotek.HireCraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateFileDTO {

    private Long id;
    private String name;
    private String description;
    private Integer orderIndex;
    private Boolean isRequired;
    private String documentType;
    private String acceptedFormats;
    private String status;
    private String filePath;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private String uploadedDate;
    private String createdDate;
    private String updatedDate;

    // Template information
    private Long templateId;
    private String templateName;

    // Candidat information
    private Long candidatId;
    private String candidatNom;
    private String candidatEmail;
    private String candidatPoste;
}