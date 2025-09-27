package ma.nexotek.HireCraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateFileTemplateDTO {

    private Long id;

    @NotBlank(message = "Le nom du template est requis")
    private String name;

    private String description;

    @NotNull(message = "L'index d'ordre est requis")
    @Min(value = 0, message = "L'index d'ordre doit être positif")
    private Integer orderIndex;

    @NotNull(message = "Le statut requis est obligatoire")
    private Boolean isRequired;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean isActive;

    private String documentType;

    private String acceptedFormats;

    private String createdDate;

    private String updatedDate;
}
