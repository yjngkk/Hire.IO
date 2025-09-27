package ma.nexotek.HireCraft.dto;

import java.time.LocalDate;

public class ResumeDto {
    private Long id;
    private String nomCandidat;
    private String poste;
    private String interviewer;
    private LocalDate dateEntretien;
    private Integer score; // 1-5
    private String recommandation;
    private String notes;
    private String PointsForts;
    private String PointsAmelioration;
    private String ProchainesEtapes;

    public ResumeDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPointsForts() {
        return PointsForts;
    }

    public void setPointsForts(String pointsForts) {
        PointsForts = pointsForts;
    }

    public String getPointsAmelioration() {
        return PointsAmelioration;
    }

    public void setPointsAmelioration(String pointsAmelioration) {
        PointsAmelioration = pointsAmelioration;
    }

    public String getProchainesEtapes() {
        return ProchainesEtapes;
    }

    public void setProchainesEtapes(String prochainesEtapes) {
        ProchainesEtapes = prochainesEtapes;
    }



    public String getNomCandidat() {
        return nomCandidat;
    }

    public void setNomCandidat(String nomCandidat) {
        this.nomCandidat = nomCandidat;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public LocalDate getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(LocalDate dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRecommandation() {
        return recommandation;
    }

    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
