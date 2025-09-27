package ma.nexotek.HireCraft.service.Onboarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.nexotek.HireCraft.dto.CandidateFileDTO;
import ma.nexotek.HireCraft.model.CandidateFile;
import ma.nexotek.HireCraft.model.CandidateFileTemplate;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.repository.CandidateFileRepository;
import ma.nexotek.HireCraft.repository.CandidateFileTemplateRepository;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ma.nexotek.HireCraft.service.EmailService.logger;

@Service
@Transactional
public class CandidateFileService {

    @Autowired
    private CandidateFileRepository candidateFileRepository;

    @Autowired
    private CandidateFileTemplateRepository templateRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ReminderService reminderService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== INITIALIZATION METHODS =====

    /**
     * Initialize files for a new candidate based on active templates
     * This should only be called once when onboarding starts
     */
    public List<CandidateFileDTO> initializeFilesForCandidat(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

        // Check if files already exist to prevent duplicate initialization
        List<CandidateFile> existingFiles = candidateFileRepository.findByCandidatIdOrderedByIndex(candidatId);
        if (!existingFiles.isEmpty()) {
            return existingFiles.stream().map(this::convertToDTO).collect(Collectors.toList());
        }

        // DEBUG: Check what templates are being retrieved
        List<CandidateFileTemplate> activeTemplates = templateRepository.findAllActiveOrderedByIndex();

        List<CandidateFile> candidateFiles = activeTemplates.stream()
                .map(template -> {
                    return new CandidateFile(template, candidat);
                })
                .collect(Collectors.toList());

        candidateFiles = candidateFileRepository.saveAll(candidateFiles);

        return candidateFiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ OPERATIONS =====

    /**
     * Get all files for a candidate
     */
    public List<CandidateFileDTO> getFilesByCandidatId(Long candidatId) {
        List<CandidateFile> files = candidateFileRepository.findByCandidatIdOrderedByIndex(candidatId);
        return files.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get file by ID
     */
    public Optional<CandidateFileDTO> getFileById(Long id) {
        return candidateFileRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get files by status for a candidate
     */
    public List<CandidateFileDTO> getFilesByStatus(Long candidatId, String status) {
        CandidateFile.FileStatus fileStatus = CandidateFile.FileStatus.valueOf(status.toUpperCase());
        return candidateFileRepository.findByCandidatIdAndStatus(candidatId, fileStatus)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get completion statistics for a candidate
     */
    public FileCompletionStats getCompletionStats(Long candidatId) {
        long totalRequired = candidateFileRepository.countRequiredFilesByCandidatId(candidatId);
        long completedRequired = candidateFileRepository.countRequiredCompletedFilesByCandidatId(candidatId);
        long totalFiles = candidateFileRepository.findByCandidatIdOrderedByIndex(candidatId).size();
        long completedFiles = candidateFileRepository.countByCandidatIdAndStatus(candidatId, CandidateFile.FileStatus.COMPLETED);
        long pendingFiles = candidateFileRepository.countByCandidatIdAndStatus(candidatId, CandidateFile.FileStatus.PENDING);
        long missingFiles = candidateFileRepository.countByCandidatIdAndStatus(candidatId, CandidateFile.FileStatus.MISSING);

        double completionPercentage = totalRequired > 0 ? (double) completedRequired / totalRequired * 100 : 0;

        return FileCompletionStats.builder()
                .totalRequired(totalRequired)
                .completedRequired(completedRequired)
                .totalFiles(totalFiles)
                .completedFiles(completedFiles)
                .pendingFiles(pendingFiles)
                .missingFiles(missingFiles)
                .completionPercentage(completionPercentage)
                .build();
    }

    // ===== CREATE OPERATIONS =====

    /**
     * Add custom file for candidate (not from template)
     * This allows candidates/admins to add additional files beyond templates
     */
    public CandidateFileDTO addCustomFile(Long candidatId, CandidateFileDTO fileDTO) {
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

        // Set order index if not provided
        Integer orderIndex = fileDTO.getOrderIndex();
        if (orderIndex == null) {
            orderIndex = getNextOrderIndex(candidatId);
        }

        CandidateFile candidateFile = CandidateFile.builder()
                .name(fileDTO.getName())
                .description(fileDTO.getDescription())
                .orderIndex(orderIndex)
                .isRequired(fileDTO.getIsRequired() != null ? fileDTO.getIsRequired() : false)
                .documentType(fileDTO.getDocumentType())
                .acceptedFormats(fileDTO.getAcceptedFormats() != null ? fileDTO.getAcceptedFormats() : ".pdf,.doc,.docx,.jpg,.jpeg,.png")
                .status(CandidateFile.FileStatus.MISSING)
                .candidat(candidat)
                .build();

        candidateFile = candidateFileRepository.save(candidateFile);
        return convertToDTO(candidateFile);
    }

    // ===== UPDATE OPERATIONS =====

    /**
     * Update file details (metadata only, not the actual file)
     */
    public CandidateFileDTO updateFile(Long fileId, CandidateFileDTO fileDTO) {
        CandidateFile existingFile = candidateFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier candidat non trouvé avec l'ID: " + fileId));

        // Update metadata
        existingFile.setName(fileDTO.getName());
        existingFile.setDescription(fileDTO.getDescription());
        existingFile.setOrderIndex(fileDTO.getOrderIndex());
        existingFile.setIsRequired(fileDTO.getIsRequired());
        existingFile.setDocumentType(fileDTO.getDocumentType());
        existingFile.setAcceptedFormats(fileDTO.getAcceptedFormats());

        existingFile = candidateFileRepository.save(existingFile);
        return convertToDTO(existingFile);
    }

    /**
     * Upload file content using DocumentService
     */
    public CandidateFileDTO uploadFile(Long fileId, MultipartFile file) {
        CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier candidat non trouvé avec l'ID: " + fileId));

        try {
            // Delete existing document if present
            if (candidateFile.getDocumentId() != null) {
                try {
                    documentService.deleteDocument(candidateFile.getDocumentId());
                } catch (Exception e) {
                    logger.warn("Failed to delete existing document: " + e.getMessage());
                }
            }

            // Upload new document using DocumentService
            Document savedDocument = documentService.uploadDocument(file);

            // Update candidate file record with document reference
            candidateFile.markAsUploaded(
                    savedDocument.getFilePath(),
                    file.getOriginalFilename(),
                    file.getSize(),
                    file.getContentType()
            );

            // Store the document ID for future reference
            candidateFile.setDocumentId(savedDocument.getId());

            candidateFile = candidateFileRepository.save(candidateFile);

            return convertToDTO(candidateFile);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage(), e);
        }
    }

    /**
     * Mark file as pending (requested from candidate)
     */
    public CandidateFileDTO markFileAsPending(Long fileId) {
        CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier candidat non trouvé avec l'ID: " + fileId));

        candidateFile.markAsPending();
        candidateFile = candidateFileRepository.save(candidateFile);
        return convertToDTO(candidateFile);
    }

    // ===== DELETE OPERATIONS =====

    /**
     * Delete file completely (metadata and physical file)
     */
    public void deleteFile(Long fileId) {
        CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier candidat non trouvé avec l'ID: " + fileId));

        // Delete associated document if exists
        if (candidateFile.getDocumentId() != null) {
            try {
                documentService.deleteDocument(candidateFile.getDocumentId());
            } catch (Exception e) {
                // Log error but don't fail the operation
                logger.error("Erreur lors de la suppression du document: " + e.getMessage());
            }
        }

        candidateFileRepository.deleteById(fileId);
    }

    /**
     * Load file as resource by candidate file ID
     */
    public org.springframework.core.io.Resource loadFileAsResource(Long fileId) {
        CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier candidat non trouvé avec l'ID: " + fileId));

        if (candidateFile.getDocumentId() == null) {
            throw new RuntimeException("Aucun document associé à ce fichier");
        }

        return documentService.loadFileAsResource(candidateFile.getDocumentId());
    }

    /**
     * Reorder files for a candidate
     */
    public List<CandidateFileDTO> reorderFiles(Long candidatId, List<Long> fileIds) {
        // Validate that all files belong to the candidate
        List<CandidateFile> files = candidateFileRepository.findAllById(fileIds);
        boolean allBelongToCandidate = files.stream()
                .allMatch(file -> file.getCandidat().getId().equals(candidatId));

        if (!allBelongToCandidate) {
            throw new RuntimeException("Tous les fichiers doivent appartenir au même candidat");
        }

        // Update order indices
        for (int i = 0; i < fileIds.size(); i++) {
            Long fileId = fileIds.get(i);
            CandidateFile file = files.stream()
                    .filter(f -> f.getId().equals(fileId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé: " + fileId));

            file.setOrderIndex(i + 1);
            candidateFileRepository.save(file);
        }

        return getFilesByCandidatId(candidatId);
    }

    /**
     * Get next available order index for a candidate
     */
    private Integer getNextOrderIndex(Long candidatId) {
        List<CandidateFile> files = candidateFileRepository.findByCandidatIdOrderedByIndex(candidatId);
        return files.stream()
                .mapToInt(CandidateFile::getOrderIndex)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Clone files from one candidate to another (useful for similar positions)
     */
    public List<CandidateFileDTO> cloneFilesFromCandidate(Long sourceCandidatId, Long targetCandidatId) {
        Candidat targetCandidat = candidatRepository.findById(targetCandidatId)
                .orElseThrow(() -> new RuntimeException("Candidat cible non trouvé avec l'ID: " + targetCandidatId));

        List<CandidateFile> sourceFiles = candidateFileRepository.findByCandidatIdOrderedByIndex(sourceCandidatId);

        List<CandidateFile> clonedFiles = sourceFiles.stream()
                .map(sourceFile -> CandidateFile.builder()
                        .name(sourceFile.getName())
                        .description(sourceFile.getDescription())
                        .orderIndex(sourceFile.getOrderIndex())
                        .isRequired(sourceFile.getIsRequired())
                        .documentType(sourceFile.getDocumentType())
                        .acceptedFormats(sourceFile.getAcceptedFormats())
                        .status(CandidateFile.FileStatus.MISSING)
                        .candidat(targetCandidat)
                        .template(sourceFile.getTemplate()) // Keep template reference if exists
                        .build())
                .collect(Collectors.toList());

        clonedFiles = candidateFileRepository.saveAll(clonedFiles);
        return clonedFiles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ===== REMINDER METHODS =====

    public ReminderResult sendFilesReminder(Long candidatId) {
        // Get candidate
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        // Validate candidate email
        if (candidat.getEmail() == null || candidat.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email du candidat non configuré");
        }

        // Get all missing and pending files
        List<CandidateFile> missingFiles = candidateFileRepository
                .findByCandidatIdAndStatus(candidatId, CandidateFile.FileStatus.MISSING);
        List<CandidateFile> pendingFiles = candidateFileRepository
                .findByCandidatIdAndStatus(candidatId, CandidateFile.FileStatus.PENDING);

        List<CandidateFile> filesToRemind = new ArrayList<>();
        filesToRemind.addAll(missingFiles);
        filesToRemind.addAll(pendingFiles);

        // Filter only required files for reminder
        List<CandidateFile> requiredFilesToRemind = filesToRemind.stream()
                .filter(CandidateFile::getIsRequired)
                .collect(Collectors.toList());

        if (!requiredFilesToRemind.isEmpty()) {
            try {
                reminderService.sendFileReminderEmail(candidat, requiredFilesToRemind);
            } catch (Exception e) {
                logger.error("Failed to send email reminder: " + e.getMessage());
                throw new RuntimeException("Impossible d'envoyer l'email de rappel: " + e.getMessage());
            }

            // Update status to pending for missing files
            missingFiles.forEach(file -> {
                if (file.getIsRequired()) {
                    file.setStatus(CandidateFile.FileStatus.PENDING);
                    candidateFileRepository.save(file);
                }
            });
        }

        return ReminderResult.builder()
                .candidatEmail(candidat.getEmail())
                .filesSent(requiredFilesToRemind.size())
                .missingFiles(missingFiles.stream().map(this::convertToDTO).collect(Collectors.toList()))
                .pendingFiles(pendingFiles.stream().map(this::convertToDTO).collect(Collectors.toList()))
                .build();
    }

    /**
     * Send reminder for a specific file
     */
    @Transactional
    public IndividualReminderResult sendIndividualFileReminder(Long fileId) {
        try {
            // Get the file
            CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            // Get the candidate
            Candidat candidat = candidateFile.getCandidat();
            if (candidat == null) {
                throw new RuntimeException("Candidat non trouvé pour ce fichier");
            }

            // Only send reminder for missing or pending files
            if (candidateFile.getStatus() == CandidateFile.FileStatus.COMPLETED) {
                throw new RuntimeException("Ce document a déjà été fourni");
            }

            logger.info("Sending individual reminder for file: {} to candidate: {}",
                    candidateFile.getName(), candidat.getEmail());

            // Send individual file reminder
            reminderService.sendIndividualFileReminder(candidat, candidateFile);

            return new IndividualReminderResult(
                    candidateFile.getName(),
                    candidat.getEmail(),
                    candidat.getNom()
            );

        } catch (Exception e) {
            logger.error("Error sending individual file reminder for file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi du rappel: " + e.getMessage(), e);
        }
    }

    /**
     * Mark file as pending and send reminder
     */
    @Transactional
    public IndividualReminderResult requestFileWithReminder(Long fileId) {
        try {
            // Get the file
            CandidateFile candidateFile = candidateFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            // Get the candidate
            Candidat candidat = candidateFile.getCandidat();
            if (candidat == null) {
                throw new RuntimeException("Candidat non trouvé pour ce fichier");
            }

            // Update status to pending if it's missing
            if (candidateFile.getStatus() == CandidateFile.FileStatus.MISSING) {
                candidateFile.setStatus(CandidateFile.FileStatus.PENDING);
                candidateFile = candidateFileRepository.save(candidateFile);
            }

            logger.info("Requesting file with reminder: {} from candidate: {}",
                    candidateFile.getName(), candidat.getEmail());

            // Send individual file reminder
            reminderService.sendIndividualFileReminder(candidat, candidateFile);

            // Convert to DTO
            CandidateFileDTO updatedFileDTO = convertToDTO(candidateFile);

            return new IndividualReminderResult(
                    candidateFile.getName(),
                    candidat.getEmail(),
                    candidat.getNom(),
                    updatedFileDTO
            );

        } catch (Exception e) {
            logger.error("Error requesting file with reminder for file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la demande: " + e.getMessage(), e);
        }
    }

    // ===== INNER CLASSES =====

    /**
     * Result object for individual reminder operations
     */
    public static class IndividualReminderResult {
        private String fileName;
        private String candidatEmail;
        private String candidatName;
        private CandidateFileDTO updatedFile;

        public IndividualReminderResult(String fileName, String candidatEmail, String candidatName) {
            this.fileName = fileName;
            this.candidatEmail = candidatEmail;
            this.candidatName = candidatName;
        }

        public IndividualReminderResult(String fileName, String candidatEmail, String candidatName, CandidateFileDTO updatedFile) {
            this.fileName = fileName;
            this.candidatEmail = candidatEmail;
            this.candidatName = candidatName;
            this.updatedFile = updatedFile;
        }

        // Getters
        public String getFileName() { return fileName; }
        public String getCandidatEmail() { return candidatEmail; }
        public String getCandidatName() { return candidatName; }
        public CandidateFileDTO getUpdatedFile() { return updatedFile; }
    }

    // Result class for the reminder operation
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderResult {
        private String candidatEmail;
        private int filesSent;
        private List<CandidateFileDTO> missingFiles;
        private List<CandidateFileDTO> pendingFiles;
    }

    // ===== CONVERSION METHODS =====

    /**
     * Convert entity to DTO
     */
    private CandidateFileDTO convertToDTO(CandidateFile candidateFile) {
        return CandidateFileDTO.builder()
                .id(candidateFile.getId())
                .name(candidateFile.getName())
                .description(candidateFile.getDescription())
                .orderIndex(candidateFile.getOrderIndex())
                .isRequired(candidateFile.getIsRequired())
                .documentType(candidateFile.getDocumentType())
                .acceptedFormats(candidateFile.getAcceptedFormats())
                .status(candidateFile.getStatus().getValue())
                .filePath(candidateFile.getFilePath())
                .originalFilename(candidateFile.getOriginalFilename())
                .fileSize(candidateFile.getFileSize())
                .mimeType(candidateFile.getMimeType())
                .uploadedDate(candidateFile.getUploadedDate() != null ? candidateFile.getUploadedDate().format(formatter) : null)
                .createdDate(candidateFile.getCreatedDate() != null ? candidateFile.getCreatedDate().format(formatter) : null)
                .updatedDate(candidateFile.getUpdatedDate() != null ? candidateFile.getUpdatedDate().format(formatter) : null)
                .templateId(candidateFile.getTemplate() != null ? candidateFile.getTemplate().getId() : null)
                .templateName(candidateFile.getTemplate() != null ? candidateFile.getTemplate().getName() : null)
                .candidatId(candidateFile.getCandidat().getId())
                .candidatNom(candidateFile.getCandidat().getNom())
                .candidatEmail(candidateFile.getCandidat().getEmail())
                .candidatPoste(candidateFile.getCandidat().getPoste())
                .build();
    }

    // ===== STATISTICS INNER CLASS =====

    @lombok.Data
    @lombok.Builder
    public static class FileCompletionStats {
        private long totalRequired;
        private long completedRequired;
        private long totalFiles;
        private long completedFiles;
        private long pendingFiles;
        private long missingFiles;
        private double completionPercentage;
    }
}