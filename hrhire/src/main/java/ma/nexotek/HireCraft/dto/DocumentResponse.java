package ma.nexotek.HireCraft.dto;

import lombok.Data;

@Data
public class DocumentResponse {
    private boolean success;
    private String message;
    private DocumentDTO document;
    private String downloadUrl;

    public DocumentResponse(boolean success, String message, DocumentDTO document) {
        this.success = success;
        this.message = message;
        this.document = document;
    }

    public DocumentResponse(boolean success, String message, DocumentDTO document, String downloadUrl) {
        this.success = success;
        this.message = message;
        this.document = document;
        this.downloadUrl = downloadUrl;
    }
}
