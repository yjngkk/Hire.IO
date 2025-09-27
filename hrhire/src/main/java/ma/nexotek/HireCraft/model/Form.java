package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Formulaire")
@Data
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String missions;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String contractType;

    @Column(nullable = false)
    private String level;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(nullable = false)
    private String tone;

    private String targetPlatform;

    private String modelUsed;

    @Column(length = 10000, columnDefinition = "TEXT") // Changez cette ligne
    private String generatedContent;

    

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String applicationLink;

    @Column(nullable = true)
    private String categorie;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean published = false;

    @Column(nullable = true)
    private LocalDateTime publishedAt;

    // Constructeurs
    public Form() {}


    public Form(String title, String missions, String location, String contractType,
                String level, String skills, String tone) {
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
    }
    public Form(String title, String missions, String location, String contractType,
                String level, String skills, String tone ,String applicationLink) {
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.applicationLink=  applicationLink;
    }



    // Constructeur avec tous les paramètres
    public Form(String title, String missions, String location, String contractType,
                String level, String skills, String tone, String targetPlatform,
                String modelUsed, String generatedContent) {
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.targetPlatform = targetPlatform;
        this.modelUsed = modelUsed;
        this.generatedContent = generatedContent;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    // Méthodes toString, equals et hashCode (optionnelles mais recommandées)
    @Override
    public String toString() {
        return "Form{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", missions='" + missions + '\'' +
                ", location='" + location + '\'' +
                ", contractType='" + contractType + '\'' +
                ", level='" + level + '\'' +
                ", skills='" + skills + '\'' +
                ", tone='" + tone + '\'' +
                ", targetPlatform='" + targetPlatform + '\'' +
                ", modelUsed='" + modelUsed + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

}