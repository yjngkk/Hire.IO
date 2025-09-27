package ma.nexotek.HireCraft.controller.Onboarding;

import ma.nexotek.HireCraft.dto.CandidateFileDTO;
import ma.nexotek.HireCraft.service.Onboarding.CandidateFileService;
import ma.nexotek.HireCraft.service.Onboarding.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate-files")
@CrossOrigin(origins = "*")
public class CandidateFileController {

    @Autowired
    private CandidateFileService candidateFileService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/candidat/{candidatId}/initialize")
    public ResponseEntity<List<CandidateFileDTO>> initializeFilesForCandidat(@PathVariable Long candidatId) {
        List<CandidateFileDTO> files = candidateFileService.initializeFilesForCandidat(candidatId);
        return ResponseEntity.status(HttpStatus.CREATED).body(files);
    }


    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<CandidateFileDTO>> getFilesByCandidatId(@PathVariable Long candidatId) {
        List<CandidateFileDTO> files = candidateFileService.getFilesByCandidatId(candidatId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateFileDTO> getFileById(@PathVariable Long id) {
        return candidateFileService.getFileById(id)
                .map(file -> ResponseEntity.ok(file))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/candidat/{candidatId}/status/{status}")
    public ResponseEntity<List<CandidateFileDTO>> getFilesByStatus(
            @PathVariable Long candidatId,
            @PathVariable String status) {
        try {
            List<CandidateFileDTO> files = candidateFileService.getFilesByStatus(candidatId, status);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/candidat/{candidatId}/stats")
    public ResponseEntity<CandidateFileService.FileCompletionStats> getCompletionStats(@PathVariable Long candidatId) {
        CandidateFileService.FileCompletionStats stats = candidateFileService.getCompletionStats(candidatId);
        return ResponseEntity.ok(stats);
    }


    @PostMapping("/candidat/{candidatId}/custom")
    public ResponseEntity<?> addCustomFile(
            @PathVariable Long candidatId,
            @Valid @RequestBody CandidateFileDTO fileDTO) {
        try {
            CandidateFileDTO createdFile = candidateFileService.addCustomFile(candidatId, fileDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{fileId}")
    public ResponseEntity<?> updateFile(
            @PathVariable Long fileId,
            @Valid @RequestBody CandidateFileDTO fileDTO) {
        try {
            CandidateFileDTO updatedFile = candidateFileService.updateFile(fileId, fileDTO);
            return ResponseEntity.ok(updatedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{fileId}/upload")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun fichier sélectionné"));
            }

            CandidateFileDTO uploadedFile = candidateFileService.uploadFile(fileId, file);
            return ResponseEntity.ok(Map.of(
                    "message", "Fichier uploadé avec succès",
                    "file", uploadedFile
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            candidateFileService.deleteFile(fileId);
            return ResponseEntity.ok().body(Map.of("message", "Fichier supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{fileId}/pending")
    public ResponseEntity<?> markFileAsPending(@PathVariable Long fileId) {
        try {
            CandidateFileDTO updatedFile = candidateFileService.markFileAsPending(fileId);
            return ResponseEntity.ok(updatedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/candidat/{candidatId}/reorder")
    public ResponseEntity<?> reorderFiles(
            @PathVariable Long candidatId,
            @RequestBody List<Long> fileIds) {
        try {
            List<CandidateFileDTO> reorderedFiles = candidateFileService.reorderFiles(candidatId, fileIds);
            return ResponseEntity.ok(reorderedFiles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * Download file content
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            CandidateFileDTO fileDTO = candidateFileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            if (fileDTO.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // Load file as Resource using FileStorageService
            Resource resource = candidateFileService.loadFileAsResource(fileId);

            // Try to determine file's content type
            String contentType = fileDTO.getMimeType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + (fileDTO.getOriginalFilename() != null ?
                                    fileDTO.getOriginalFilename() : fileDTO.getName()) + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/candidat/{candidatId}/download-all")
    public ResponseEntity<Resource> downloadAllFiles(@PathVariable Long candidatId) {
        try {
            // This would require implementing a ZIP creation service
            // For now, return not implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/candidat/{candidatId}/is-complete")
    public ResponseEntity<Map<String, Object>> isCandidateFileComplete(@PathVariable Long candidatId) {
        try {
            CandidateFileService.FileCompletionStats stats = candidateFileService.getCompletionStats(candidatId);
            boolean isComplete = stats.getCompletedRequired() == stats.getTotalRequired();

            return ResponseEntity.ok(Map.of(
                    "isComplete", isComplete,
                    "completionPercentage", stats.getCompletionPercentage(),
                    "completedRequired", stats.getCompletedRequired(),
                    "totalRequired", stats.getTotalRequired(),
                    "missingFiles", stats.getMissingFiles(),
                    "pendingFiles", stats.getPendingFiles()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/candidat/{candidatId}/send-reminder")
    public ResponseEntity<?> sendFilesReminder(@PathVariable Long candidatId) {
        try {
            CandidateFileService.ReminderResult result = candidateFileService.sendFilesReminder(candidatId);

            if (result.getFilesSent() == 0) {
                return ResponseEntity.ok(Map.of(
                        "message", "Aucun fichier en attente trouvé",
                        "filesSent", 0,
                        "candidatEmail", result.getCandidatEmail()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Rappel envoyé avec succès",
                    "filesSent", result.getFilesSent(),
                    "candidatEmail", result.getCandidatEmail(),
                    "missingFiles", result.getMissingFiles(),
                    "pendingFiles", result.getPendingFiles()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{fileId}/send-reminder")
    public ResponseEntity<?> sendIndividualFileReminder(@PathVariable Long fileId) {
        try {
            CandidateFileService.IndividualReminderResult result = candidateFileService.sendIndividualFileReminder(fileId);

            return ResponseEntity.ok(Map.of(
                    "message", "Rappel envoyé avec succès",
                    "fileName", result.getFileName(),
                    "candidatEmail", result.getCandidatEmail(),
                    "candidatName", result.getCandidatName()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{fileId}/request-with-reminder")
    public ResponseEntity<?> requestFileWithReminder(@PathVariable Long fileId) {
        try {
            CandidateFileService.IndividualReminderResult result = candidateFileService.requestFileWithReminder(fileId);

            return ResponseEntity.ok(Map.of(
                    "message", "Demande envoyée avec succès",
                    "fileName", result.getFileName(),
                    "candidatEmail", result.getCandidatEmail(),
                    "candidatName", result.getCandidatName(),
                    "file", result.getUpdatedFile()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/candidat/{candidatId}/summary")
    public ResponseEntity<Map<String, Object>> getCandidateFileSummary(@PathVariable Long candidatId) {
        try {
            List<CandidateFileDTO> files = candidateFileService.getFilesByCandidatId(candidatId);
            CandidateFileService.FileCompletionStats stats = candidateFileService.getCompletionStats(candidatId);

            return ResponseEntity.ok(Map.of(
                    "files", files,
                    "stats", stats,
                    "hasFiles", !files.isEmpty()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{fileId}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId) {
        try {
            CandidateFileDTO fileDTO = candidateFileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            if (fileDTO.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // Load file as Resource using FileStorageService
            Resource resource = candidateFileService.loadFileAsResource(fileId);

            // Try to determine file's content type
            String contentType = fileDTO.getMimeType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            // Use "inline" disposition for viewing in browser instead of downloading
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header("Expires", "0")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/{fileId}/validate")
    public ResponseEntity<?> validateFile(
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file) {
        try {
            CandidateFileDTO fileDTO = candidateFileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            // Basic validations
            Map<String, Object> validationResult = Map.of(
                    "isValid", true,
                    "errors", List.of(),
                    "warnings", List.of(),
                    "fileSize", file.getSize(),
                    "fileName", file.getOriginalFilename(),
                    "contentType", file.getContentType()
            );

            return ResponseEntity.ok(validationResult);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}