package ma.nexotek.HireCraft.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String missions;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Contract type is required")
    private String contractType;

    @NotBlank(message = "Level is required")
    private String level;

    private String skills;

    @NotBlank(message = "Tone is required")
    private String tone;



    private String applicationLink;
    private String categorie;
    public FormRequestDTO() {}

    public FormRequestDTO(String title, String missions, String location, String contractType, String level, String skills, String tone, String applicationLink) {
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.applicationLink = applicationLink;
    }



}