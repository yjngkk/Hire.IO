package ma.nexotek.HireCraft.dto;

import java.time.LocalDateTime;

public class FormResponseDTO {

    private Long id;
    private String title;
    private String missions;
    private String location;
    private String contractType;
    private String level;
    private String skills;
    private String tone;

    private String applicationLink;
    private LocalDateTime createdAt;
    private Boolean published;
    private LocalDateTime publishedAt;

    // Constructors
    public FormResponseDTO() {}

    public FormResponseDTO(Long id, String title, String missions, String location,
                           String contractType, String level, String skills,
                           String tone, LocalDateTime createdAt ,String applicationLink) {
        this.id = id;
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.createdAt = createdAt;
        this.applicationLink=applicationLink;
    }

    public FormResponseDTO(Long id, String title, String missions, String location,
                           String contractType, String level, String skills,
                           String tone, LocalDateTime createdAt, String applicationLink,
                           Boolean published, LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.missions = missions;
        this.location = location;
        this.contractType = contractType;
        this.level = level;
        this.skills = skills;
        this.tone = tone;
        this.createdAt = createdAt;
        this.applicationLink = applicationLink;
        this.published = published;
        this.publishedAt = publishedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMissions() {
        return missions;
    }

    public void setMissions(String missions) {
        this.missions = missions;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
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
}
