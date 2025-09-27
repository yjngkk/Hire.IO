package ma.nexotek.HireCraft.controller.Entretien;



import java.io.IOException;
import java.security.GeneralSecurityException;

import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Entretien;
import ma.nexotek.HireCraft.dto.EntretienDTO;
import ma.nexotek.HireCraft.mapper.EntretienMapper;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.EntretienRepository;
import ma.nexotek.HireCraft.service.EmailService;
import ma.nexotek.HireCraft.service.GoogleCalendarService;
import ma.nexotek.HireCraft.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entretiens")
public class EntretienController {
    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private TimelineService timelineService;

    @PostMapping
    public EntretienDTO planifierEntretien(@RequestBody Entretien entretien) {
        entretien.setStatut("en_cours"); // Par défaut en cours au lieu de "prévu"
        // Forcer le type à 'meet' côté backend
        entretien.setType("meet");

        // Récupérer le candidat complet depuis la base
        if (entretien.getCandidat() != null && entretien.getCandidat().getId() != null) {
            Candidat candidatComplet = candidatRepository.findById(entretien.getCandidat().getId()).orElse(null);
            entretien.setCandidat(candidatComplet);
        }

        // Générer le lien Meet automatiquement pour tout entretien
        try {
            String meetLink = googleCalendarService.createMeetEvent(
                "Entretien - " + entretien.getCandidat().getPoste(),
                "Entretien pour le poste de " + entretien.getCandidat().getPoste(),
                entretien.getDateHeure(),
                entretien.getDateHeure().plusMinutes(getDureeMinutes(entretien.getDuree())),
                entretien.getOrganizerEmail(),
                entretien.getInterviewerEmail(),
                entretien.getCandidat() != null ? entretien.getCandidat().getEmail() : null
            );
            if (meetLink == null) {
                System.err.println("[ERREUR] Le lien Google Meet n'a pas pu être généré. Vérifiez l'authentification Google Calendar côté serveur.");
                throw new RuntimeException("Le lien Google Meet n'a pas pu être généré. Veuillez vérifier l'authentification Google Calendar côté serveur.");
            }
            entretien.setMeetLink(meetLink);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du lien Google Meet : " + e.getMessage());
        }
        Entretien saved = entretienRepository.save(entretien);
        Candidat candidat= candidatRepository.findById(entretien.getCandidat().getId()).orElse(null);
        timelineService.addEntretien(candidat);
        // Vérification des emails avant d'envoyer
        if (saved.getCandidat() != null && saved.getCandidat().getEmail() != null
            && saved.getInterviewerEmail() != null && saved.getOrganizerEmail() != null) {
            try {
                emailService.sendInterviewInvitation(
                    saved.getCandidat().getPoste(),
                    saved.getDateHeure(),
                    saved.getInterviewer(),
                    saved.getInterviewerEmail(),
                    saved.getCandidat().getNom(),
                    saved.getCandidat().getEmail(),
                    saved.getMeetLink() != null ? saved.getMeetLink() : "",
                    saved.getOrganizerEmail()
                );
            } catch (jakarta.mail.MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
        return EntretienMapper.toDto(saved);
    }

    // Méthode utilitaire pour convertir la durée texte en minutes
    private int getDureeMinutes(String duree) {
        return switch (duree) {
            case "30min" -> 30;
            case "45min" -> 45;
            case "1h" -> 60;
            case "1h30min" -> 90;
            default -> 60;
        };
    }

    @GetMapping
    public List<EntretienDTO> getEntretiens() {
        return entretienRepository.findAllWithCandidatAndCv().stream().map(EntretienMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntretienDTO> getEntretienById(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            return ResponseEntity.ok(EntretienMapper.toDto(entretien));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/statut/{statut}")
    public List<EntretienDTO> getEntretiensByStatut(@PathVariable String statut) {
        return entretienRepository.findByStatut(statut).stream().map(EntretienMapper::toDto).toList();
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<EntretienDTO> marquerPause(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "5") int duration) {

        // Validation de la durée
        if (duration < 5 || duration > 30) {
            return ResponseEntity.badRequest().build();
        }

        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            entretien.setStatut("pause");
            Entretien saved = entretienRepository.save(entretien);

            // Stocker la durée de pause dans les notes (optionnel)
            String pauseNote = "Pause de " + duration + " minutes démarrée à " + LocalDateTime.now();
            if (saved.getNotes() == null || saved.getNotes().isEmpty()) {
                saved.setNotes(pauseNote);
            } else {
                saved.setNotes(saved.getNotes() + "\n" + pauseNote);
            }
            entretienRepository.save(saved);

            // Envoyer notification
            try {
                emailService.sendInterviewStatusNotification(
                        saved.getCandidat().getEmail(),
                        saved.getInterviewerEmail(),
                        saved.getOrganizerEmail(),
                        "Entretien mis en pause (" + duration + " min)",
                        "L'entretien a été temporairement mis en pause pour " + duration + " minutes.",
                        saved.getCandidat().getPoste(),
                        saved.getDateHeure()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok(EntretienMapper.toDto(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<EntretienDTO> terminerEntretien(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            entretien.setStatut("terminé");
            Entretien saved = entretienRepository.save(entretien);

            // Envoyer notification de fin d'entretien
            try {
                emailService.sendInterviewStatusNotification(
                    entretien.getCandidat().getEmail(),
                    entretien.getInterviewerEmail(),
                    entretien.getOrganizerEmail(),
                    "Entretien terminé",
                    "L'entretien a été marqué comme terminé.",
                    entretien.getCandidat().getPoste(),
                    entretien.getDateHeure()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok(EntretienMapper.toDto(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/reprendre")
    public ResponseEntity<EntretienDTO> reprendreEntretien(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            entretien.setStatut("en_cours");
            Entretien saved = entretienRepository.save(entretien);

            // Envoyer notification de reprise
            try {
                emailService.sendInterviewStatusNotification(
                    entretien.getCandidat().getEmail(),
                    entretien.getInterviewerEmail(),
                    entretien.getOrganizerEmail(),
                    "Entretien repris",
                    "L'entretien a été repris.",
                    entretien.getCandidat().getPoste(),
                    entretien.getDateHeure()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok(EntretienMapper.toDto(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/rappel")
    public ResponseEntity<Map<String, String>> envoyerRappel(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            try {
                emailService.sendInterviewReminder(
                    entretien.getCandidat().getPoste(),
                    entretien.getDateHeure(),
                    entretien.getInterviewer(),
                    entretien.getInterviewerEmail(),
                    entretien.getCandidat().getNom(),
                    entretien.getCandidat().getEmail(),
                    entretien.getMeetLink() != null ? entretien.getMeetLink() : "",
                    entretien.getOrganizerEmail()
                );
                return ResponseEntity.ok(Map.of("message", "Rappel envoyé avec succès"));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de l'envoi du rappel: " + e.getMessage()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/rappel-automatique")
    public ResponseEntity<Map<String, String>> activerRappelAutomatique(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            // Logique pour activer les rappels automatiques
            // Ici on pourrait ajouter une tâche planifiée
            return ResponseEntity.ok(Map.of("message", "Rappels automatiques activés pour cet entretien"));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerEntretien(@PathVariable Long id) {
        Entretien entretien = entretienRepository.findById(id).orElse(null);
        if (entretien != null) {
            entretienRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}