package ma.nexotek.HireCraft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobOfferRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @JsonProperty("title")
    private String title;
    
    @NotBlank(message = "Les missions sont obligatoires")
    @JsonProperty("missions")
    private String missions;
    
    @NotBlank(message = "Le lieu est obligatoire")
    @JsonProperty("location")
    private String location;
    
    @NotBlank(message = "Le type de contrat est obligatoire")
    @JsonProperty("contractType")
    private String contractType;
    
    @NotBlank(message = "Le niveau est obligatoire")
    @JsonProperty("level")
    private String level;
    
    @NotBlank(message = "Les compétences sont obligatoires")
    @JsonProperty("skills")
    private String skills;
    
    @NotBlank(message = "Le ton est obligatoire")
    @JsonProperty("tone")
    private String tone;
    
    @JsonProperty("targetPlatform")
    private String targetPlatform = "linkedin";
    
    @JsonProperty("preferredModel")
    private String preferredModel = "mistral";

    @JsonProperty("categorie")
    private String categorie;

    // Constructeurs
    public JobOfferRequest() {}

    public JobOfferRequest(String title, String missions, String location, String contractType,
                          String level, String skills, String tone, String targetPlatform, 
                          String preferredModel) {
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.targetPlatform = targetPlatform;
        this.preferredModel = preferredModel;
    }
}