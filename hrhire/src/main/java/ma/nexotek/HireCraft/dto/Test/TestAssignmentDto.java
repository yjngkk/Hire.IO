package ma.nexotek.HireCraft.dto.Test;

import ma.nexotek.HireCraft.dto.CandidatDTO;
import ma.nexotek.HireCraft.model.Test;

import java.time.LocalDateTime;

public class TestAssignmentDto {
    private Long assignmentId;
    private String status;
    private String accessToken;
    private LocalDateTime sentAt;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;

    private CandidatDTO candidat;

    private Test test; ;


    public TestAssignmentDto() {}

    public TestAssignmentDto(Long assignmentId, String status, String accessToken,
                             LocalDateTime sentAt, LocalDateTime startedAt, LocalDateTime expiresAt,
                             CandidatDTO candidat, Test test) {
        this.assignmentId = assignmentId;
        this.status = status;
        this.accessToken = accessToken;
        this.sentAt = sentAt;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.candidat = candidat;
        this.test = test;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public CandidatDTO getCandidat() {
        return candidat;
    }

    public void setCandidat(CandidatDTO candidat) {
        this.candidat = candidat;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
