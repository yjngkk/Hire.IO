package ma.nexotek.HireCraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Form;

@Data

public class CandidatDTO {

    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String poste;
    private String notes;
    private DocumentDTO cv;
    private Double cvScore;
    private String processStatus;
    private String formTitre;
    private Boolean bonTalent;
    private String createdAt;

    // Constructeur depuis entité
    public CandidatDTO(Candidat candidat) {
        this.id = candidat.getId();
        this.nom = candidat.getNom();
        this.email = candidat.getEmail();
        this.telephone = candidat.getTelephone();
        this.poste = candidat.getPoste();
        this.notes = candidat.getNotes();
        this.cvScore = candidat.getCvScore();
        this.processStatus = candidat.getProcessStatus();
       // this.createdAt = candidat.getCreatedAt().toString();
        this.createdAt = candidat.getCreatedAt() != null ?
                candidat.getCreatedAt().toString() :
                null;

        this.bonTalent = candidat.getBonTalent();

        // Null-safe mapping for offre title to prevent NullPointerException
        this.formTitre = (candidat.getOffre() != null) ? candidat.getOffre().getTitle() : null;

        // Add null check for CV to prevent potential issues
        if (candidat.getCv() != null) {
            try {
                this.cv = new DocumentDTO(candidat.getCv());
            } catch (Exception e) {
                // Handle LOB access issues gracefully
                this.cv = null;
            }
        }
    }

}
