package ma.nexotek.HireCraft.controller;

import ma.nexotek.HireCraft.dto.DocumentDTO;
import ma.nexotek.HireCraft.dto.DocumentResponse;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.DocumentRepository;
import ma.nexotek.HireCraft.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    @Autowired
    private CandidatRepository candidatRepository;
    @Autowired
    private DocumentRepository documentRepository;


    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }


    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new DocumentResponse(false, "File is empty", null));
            }

            Document savedDocument = documentService.uploadDocument(file);

            String downloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/documents/download/")
                    .path(savedDocument.getId().toString())
                    .toUriString();

            return ResponseEntity.ok(new DocumentResponse(true,
                    "Document uploaded successfully", new DocumentDTO(savedDocument), downloadUri));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DocumentResponse(false, "Upload failed: " + ex.getMessage(), null));
        }
    }

    // READ - Get all documents
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentDTOs);
    }

    // READ - Get document by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocument(id);
            DocumentDTO documentDTO = new DocumentDTO(document);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // READ - Search documents by name
    @GetMapping("/search")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(@RequestParam String name) {
        List<Document> documents = documentService.searchByName(name);
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentDTOs);
    }

    // READ - Get documents by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByType(@PathVariable String type) {
        List<Document> documents = documentService.getByType(type);
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentDTOs);
    }

    // UPDATE - Update document name
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id, @RequestParam String name) {
        try {
            Document updatedDocument = documentService.updateDocument(id, name);
            DocumentDTO documentDTO = new DocumentDTO(updatedDocument);
            return ResponseEntity.ok(documentDTO);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Delete document
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        try {
            boolean deleted = documentService.deleteDocument(id);
            if (deleted) {
                return ResponseEntity.ok("Document deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Delete failed: " + ex.getMessage());
        }
    }

    // DOWNLOAD from database content
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "attachment") String disposition) {
        try {
            Document document = documentService.getDocument(id);
            HttpHeaders headers = new HttpHeaders();

            // Ensure proper content type
            String contentType = document.getType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(document.getContent().length);

            // Clean filename - remove special characters that might cause issues
            String cleanFilename = document.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + cleanFilename + "\"");

            // Add cache control headers
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(document.getContent(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            System.err.println("Error with document: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // DOWNLOAD from file system
    @GetMapping("/download-file/{id}")
    public ResponseEntity<Resource> downloadDocumentFromFile(@PathVariable Long id) {
        try {
            Document document = documentService.getDocument(id);
            Resource resource = documentService.loadFileAsResource(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getName() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocument(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getType()));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getName() + "\"");

            return new ResponseEntity<>(document.getContent(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{candidatId}/cv/download")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadCandidateCV(@PathVariable Long candidatId) {
        try {
            // Get the candidate
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

            // Check if candidate has a CV
            Document cv = candidat.getCv();
            if (cv == null) {
                return ResponseEntity.notFound().build();
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();

            String contentType = cv.getType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/pdf"; // Default to PDF
            }

            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(cv.getContent().length);

            // Clean filename
            String filename = "CV_" + candidat.getNom().replaceAll("[^a-zA-Z0-9._-]", "_") +
                    getFileExtension(contentType);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            // Cache control
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(cv.getContent(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            System.err.println("Error downloading CV: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{candidatId}/cv/view")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> viewCandidateCV(@PathVariable Long candidatId) {
        try {
            // Get the candidate
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

            // Check if candidate has a CV
            Document cv = candidat.getCv();
            if (cv == null) {
                return ResponseEntity.notFound().build();
            }

            // Prepare headers for inline viewing
            HttpHeaders headers = new HttpHeaders();

            String contentType = cv.getType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/pdf";
            }

            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");

            // Cache control
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(cv.getContent(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            System.err.println("Error viewing CV: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{candidatId}/cv/info")
    public ResponseEntity<?> getCandidateCVInfo(@PathVariable Long candidatId) {
        try {
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            Document cv = candidat.getCv();
            if (cv == null) {
                return ResponseEntity.ok(Map.of(
                        "hasCV", false,
                        "message", "Aucun CV disponible pour ce candidat"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "hasCV", true,
                    "cvId", cv.getId(),
                    "filename", cv.getName(),
                    "size", cv.getSize(),
                    "contentType", cv.getType(),
                    "uploadDate", cv.getUploadDate()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String getFileExtension(String contentType) {
        if (contentType == null) return ".pdf";

        switch (contentType.toLowerCase()) {
            case "application/pdf":
                return ".pdf";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            default:
                return ".pdf";
        }
    }

    @PostMapping("/{candidatId}/cv/upload")
    public ResponseEntity<?> uploadCandidateCV(
            @PathVariable Long candidatId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validation
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Aucun fichier sélectionné"));
            }
            // Validate file size (max 10MB for CV)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le fichier est trop volumineux (max 10MB)"));
            }
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") &&
                    !contentType.equals("application/msword") &&
                    !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Type de fichier non supporté. Utilisez PDF, DOC ou DOCX"));
            }
            // Get the candidate
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));
            byte[] fileContent = file.getBytes();
            if (fileContent.length == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le contenu du fichier est vide"));
            }
            // Create or get existing CV document
            Document cv = candidat.getCv();
            if (cv != null) {
                System.out.println("Updating existing CV with ID: " + cv.getId());
            } else {
                cv = new Document();
                System.out.println("Creating new CV document");
            }

            // Set document properties - MAKE SURE ALL FIELDS ARE SET
            String filename = "CV_" + candidat.getNom().replaceAll("[^a-zA-Z0-9._-]", "_") +
                    "_" + file.getOriginalFilename();

            cv.setName(filename);
            cv.setType(contentType);
            cv.setSize(file.getSize());
            cv.setContent(fileContent); // ← THIS IS CRITICAL!
            cv.setFilePath("cv/" + candidatId + "/" + file.getOriginalFilename());
            cv.setUploadDate(LocalDateTime.now());
            Document savedCV;
            try {
                savedCV = documentService.uploadDocument(file);
                savedCV.setName(filename);
                savedCV.setFilePath("cv/" + candidatId + "/" + file.getOriginalFilename());
                savedCV = documentRepository.save(savedCV);
            } catch (Exception e) {
                savedCV = documentRepository.save(cv);
            }
            if (candidat.getCv() == null || !candidat.getCv().getId().equals(savedCV.getId())) {
                candidat.setCv(savedCV);
                candidatRepository.save(candidat);
                System.out.println("CV linked to candidate");
            }

            // VERIFICATION STEP - Re-fetch and verify content was saved
            Document verifyCV = documentService.getDocument(savedCV.getId());
            int verifyContentLength = verifyCV.getContent() != null ? verifyCV.getContent().length : 0;
            if (verifyContentLength == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Le contenu du CV n'a pas été sauvegardé correctement"));
            }
            if (verifyContentLength != fileContent.length) {
            }
            return ResponseEntity.ok(Map.of(
                    "message", "CV uploadé avec succès",
                    "cvId", savedCV.getId(),
                    "filename", savedCV.getName(),
                    "originalSize", file.getSize(),
                    "savedContentLength", verifyContentLength,
                    "uploadDate", savedCV.getUploadDate()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload: " + e.getMessage()));
        }
    }


}
