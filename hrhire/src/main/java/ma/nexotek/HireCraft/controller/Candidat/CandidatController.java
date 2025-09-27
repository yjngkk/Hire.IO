package ma.nexotek.HireCraft.controller.Candidat;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.CandidatDTO;
import ma.nexotek.HireCraft.dto.CandidatRequest;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.service.CandidatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidats")
@Slf4j
public class CandidatController {
    @Autowired
    private CandidatRepository candidatRepository;
//    private final DocumentService documentService;
    @Autowired
   private  CandidatService candidatService;


    @PostMapping
    public Candidat createCandidat(@RequestBody Candidat candidat) {
        return candidatRepository.save(candidat);
    }


    @PostMapping("/with-cv")
    public ResponseEntity<CandidatDTO> createCandidatWithCv(
            @ModelAttribute @Valid CandidatRequest candidatRequest,
            @RequestParam("cv")  MultipartFile cvFile) {

        log.info("Received candidat request: {}", candidatRequest);
        log.info("CV file: name={}, size={}, type={}",
                cvFile.getOriginalFilename(), cvFile.getSize(), cvFile.getContentType());

        CandidatDTO createdCandidat = candidatService.createCandidat(candidatRequest, cvFile);
        return new ResponseEntity<>(createdCandidat, HttpStatus.CREATED);
    }

    // READ - Récupérer tous les candidats
    @GetMapping
    public ResponseEntity<List<CandidatDTO>> getAllCandidats() {
        List<CandidatDTO> candidats = candidatService.getAllCandidats();
        return ResponseEntity.ok(candidats);
    }

    // READ - Récupérer uniquement les bons talents
    @GetMapping("/good-talents")
    public ResponseEntity<List<CandidatDTO>> getGoodTalents() {
        List<CandidatDTO> candidats = candidatService.getGoodTalents();
        return ResponseEntity.ok(candidats);
    }

    // READ - Récupérer un candidat par ID
    @GetMapping("/{id}")
    public ResponseEntity<CandidatDTO> getCandidatById(@PathVariable Long id) {
        CandidatDTO candidat = candidatService.getCandidatById(id);
        return ResponseEntity.ok(candidat);
    }

    // UPDATE - Mettre à jour un candidat (avec nouveau CV optionnel)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<CandidatDTO> updateCandidat(
            @PathVariable Long id,
            @RequestPart("candidat") @Valid CandidatRequest candidatRequest,
            @RequestPart(value = "cv", required = false) MultipartFile cvFile) {

        CandidatDTO updatedCandidat = candidatService.updateCandidat(id, candidatRequest, cvFile);
        return ResponseEntity.ok(updatedCandidat);
    }

    // UPDATE - Mettre à jour uniquement les infos (sans CV)
    @PutMapping("/{id}/info")
    public ResponseEntity<CandidatDTO> updateCandidatInfo(
            @PathVariable Long id,
            @RequestBody @Valid CandidatRequest candidatRequest) {

        CandidatDTO updatedCandidat = candidatService.updateCandidatInfo(id, candidatRequest);
        return ResponseEntity.ok(updatedCandidat);
    }

    // DELETE - Supprimer un candidat
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidat(@PathVariable Long id) {
        candidatService.deleteCandidat(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint pour télécharger le CV
    @GetMapping("/{id}/cv/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        return candidatService.downloadCv(id);
    }


    @GetMapping("/{candidatId}/cv/download")
    public ResponseEntity<byte[]> downloadCandidateCV(@PathVariable Long candidatId) {
        try {
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

            Document cv = candidat.getCv();
            if (cv == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            String contentType = cv.getType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/pdf";
            }

            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(cv.getContent().length);

            String filename = "CV_" + candidat.getNom().replaceAll("[^a-zA-Z0-9._-]", "_") + ".pdf";
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return new ResponseEntity<>(cv.getContent(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            System.err.println("Error downloading CV: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{candidatId}/cv/view")
    public ResponseEntity<byte[]> viewCandidateCV(@PathVariable Long candidatId) {
        try {
            Candidat candidat = candidatRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));

            Document cv = candidat.getCv();
            if (cv == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            String contentType = cv.getType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/pdf";
            }

            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");
            headers.setCacheControl("no-cache, no-store, must-revalidate");

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


    @GetMapping("/weekly-stats")
    public ResponseEntity<Map<String, Integer>> getWeeklyCandidatStats() {
        try {
            Map<String, Integer> stats = candidatService.getWeeklyCandidatStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

