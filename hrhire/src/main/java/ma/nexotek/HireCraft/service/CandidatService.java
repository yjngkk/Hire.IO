package ma.nexotek.HireCraft.service;

import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.CandidatDTO;
import ma.nexotek.HireCraft.dto.CandidatRequest;
import ma.nexotek.HireCraft.exeption.ResourceNotFoundException;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service

@Slf4j
@Transactional
public class CandidatService {
    @Autowired
    private  CandidatRepository candidatRepository;
    @Autowired
    private  DocumentService documentService;

    @Autowired
    private AutomatedProcessService automatedProcessService;

    @Autowired
    private  FormRepository formRepository;

    @Autowired
    private TimelineService  timelineService;

    private static final List<String> ALLOWED_CV_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public CandidatDTO createCandidat(CandidatRequest candidatRequest, MultipartFile cvFile) {
        // Validation du fichier CV
        validateCvFile(cvFile);

        // Créer l'entité Candidat
        Candidat candidat = new Candidat();
        candidat.setNom(candidatRequest.getNom());
        candidat.setEmail(candidatRequest.getEmail());
        candidat.setTelephone(candidatRequest.getTelephone());
        candidat.setPoste(candidatRequest.getPoste());
        candidat.setNotes(candidatRequest.getNotes());
        candidat.setBonTalent(Boolean.TRUE.equals(candidatRequest.getBonTalent()));
        candidat.setProcessStatus("CREATED"); // NOUVEAU

        if (candidatRequest.getOffreId() != null) {
            Form offre = formRepository.findById(candidatRequest.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable avec l'ID: " + candidatRequest.getOffreId()));
            candidat.setOffre(offre);
        }
        // Sauvegarder le fichier CV via DocumentService
        Document cvDocument = documentService.uploadDocument(cvFile);
        candidat.setCv(cvDocument);

        // Sauvegarder le candidat
        Candidat savedCandidat = candidatRepository.save(candidat);
        timelineService.addCandidatureReceived(savedCandidat);

        if (candidatRequest.getOffreId() != null) {
            automatedProcessService.processNewCandidat(savedCandidat, candidatRequest.getOffreId());

            // Recharger le candidat pour avoir les données mises à jour
            savedCandidat = candidatRepository.findById(savedCandidat.getId()).orElse(savedCandidat);
        }

        return new CandidatDTO(savedCandidat);
    }

    @Transactional(readOnly = true)
    public List<CandidatDTO> getAllCandidats() {
        return candidatRepository.findAll()
                .stream()
                .map(CandidatDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidatDTO> getGoodTalents() {
        return candidatRepository.findByBonTalentTrue()
                .stream()
                .map(CandidatDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CandidatDTO getCandidatById(Long id) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + id));
        return new CandidatDTO(candidat);
    }

    public CandidatDTO updateCandidat(Long id, CandidatRequest candidatRequest, MultipartFile cvFile) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + id));

        // Mettre à jour les informations de base
        candidat.setNom(candidatRequest.getNom());
        candidat.setEmail(candidatRequest.getEmail());
        candidat.setTelephone(candidatRequest.getTelephone());
        candidat.setPoste(candidatRequest.getPoste());
        candidat.setNotes(candidatRequest.getNotes());
        if (candidatRequest.getBonTalent() != null) {
            candidat.setBonTalent(candidatRequest.getBonTalent());
        }

        // Mettre à jour le CV si fourni
        if (cvFile != null && !cvFile.isEmpty()) {
            validateCvFile(cvFile);

            // Supprimer l'ancien CV
            if (candidat.getCv() != null) {
                documentService.deleteDocument(candidat.getCv().getId());
            }

            // Sauvegarder le nouveau CV
            Document newCvDocument = documentService.uploadDocument(cvFile);
            candidat.setCv(newCvDocument);
        }

        Candidat updatedCandidat = candidatRepository.save(candidat);
        return new CandidatDTO(updatedCandidat);
    }

    public CandidatDTO updateCandidatInfo(Long id, CandidatRequest candidatRequest) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + id));

        candidat.setNom(candidatRequest.getNom());
        candidat.setEmail(candidatRequest.getEmail());
        candidat.setTelephone(candidatRequest.getTelephone());
        candidat.setPoste(candidatRequest.getPoste());
        candidat.setNotes(candidatRequest.getNotes());
        if (candidatRequest.getBonTalent() != null) {
            candidat.setBonTalent(candidatRequest.getBonTalent());
        }

        Candidat updatedCandidat = candidatRepository.save(candidat);
        return new CandidatDTO(updatedCandidat);
    }

    public void deleteCandidat(Long id) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + id));

        // Supprimer le fichier CV via DocumentService
        if (candidat.getCv() != null) {
            documentService.deleteDocument(candidat.getCv().getId());
        }

        candidatRepository.delete(candidat);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadCv(Long id) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + id));

        if (candidat.getCv() == null) {
            throw new ResourceNotFoundException("Aucun CV trouvé pour ce candidat");
        }

        try {
            Resource resource = documentService.loadFileAsResource(candidat.getCv().getId());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(candidat.getCv().getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + candidat.getCv().getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CV", e);
        }
    }

    private void validateCvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CV est obligatoire");
        }

        if (!ALLOWED_CV_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Seuls PDF, DOC et DOCX sont acceptés");
        }

        // Limite de 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Le fichier CV ne doit pas dépasser 10MB");
        }
    }
    // Add this method to your CandidatService.java

    public Map<String, Integer> getWeeklyCandidatStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        // Get candidates created this week
        List<Candidat> thisWeekCandidats = candidatRepository
                .findByCreatedAtBetween(oneWeekAgo, now);

        // Get candidates created last week for comparison
        List<Candidat> lastWeekCandidats = candidatRepository
                .findByCreatedAtBetween(twoWeeksAgo, oneWeekAgo);

        int thisWeekCount = thisWeekCandidats.size();
        int lastWeekCount = lastWeekCandidats.size();

        // Calculate percentage change
        int percentageChange = 0;
        if (lastWeekCount > 0) {
            percentageChange = Math.round(((float)(thisWeekCount - lastWeekCount) / lastWeekCount) * 100);
        } else if (thisWeekCount > 0) {
            percentageChange = 100; // If no candidates last week but some this week, it's 100% increase
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("thisWeekCreated", thisWeekCount);
        stats.put("lastWeekCreated", lastWeekCount);
        stats.put("percentageChange", percentageChange);

        return stats;
    }

}
