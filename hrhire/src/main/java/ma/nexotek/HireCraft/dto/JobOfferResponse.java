package ma.nexotek.HireCraft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class JobOfferResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("formId")
    private Long formId;
    
    @JsonProperty("generatedContent")
    private String generatedContent;
    
    @JsonProperty("targetPlatform")
    private String targetPlatform;
    
    @JsonProperty("modelUsed")
    private String modelUsed;
    
    @JsonProperty("wordCount")
    private Integer wordCount;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("generationTimestamp")
    private LocalDateTime generationTimestamp;
    
    @JsonProperty("error")
    private String error;

    // Constructeurs
    public JobOfferResponse() {
        this.generationTimestamp = LocalDateTime.now();
    }

    public JobOfferResponse(boolean success, Long formId, String generatedContent, 
                           String targetPlatform, String modelUsed, Long processingTimeMs) {
        this();
        this.success = success;
        this.formId = formId;
        this.generatedContent = generatedContent;
        this.targetPlatform = targetPlatform;
        this.modelUsed = modelUsed;
        this.processingTimeMs = processingTimeMs;
        
        if (generatedContent != null) {
            this.wordCount = generatedContent.split("\\s+").length;
        }
    }

    // Factory methods pour une API fluide
    public static JobOfferResponse success(Long formId, String generatedContent, 
                                         String modelUsed, String targetPlatform, 
                                         long processingTimeMs) {
        JobOfferResponse response = new JobOfferResponse(true, formId, generatedContent, 
                                                        targetPlatform, modelUsed, processingTimeMs);
        return response;
    }
    
    public static JobOfferResponse error(String errorMessage) {
        JobOfferResponse response = new JobOfferResponse();
        response.setSuccess(false);
        response.setError(errorMessage);
        return response;
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getGeneratedContent() { return generatedContent; }
    public void setGeneratedContent(String generatedContent) { 
        this.generatedContent = generatedContent;
        if (generatedContent != null) {
            this.wordCount = generatedContent.split("\\s+").length;
        }
    }

    public String getTargetPlatform() { return targetPlatform; }
    public void setTargetPlatform(String targetPlatform) { this.targetPlatform = targetPlatform; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public LocalDateTime getGenerationTimestamp() { return generationTimestamp; }
    public void setGenerationTimestamp(LocalDateTime generationTimestamp) { this.generationTimestamp = generationTimestamp; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}