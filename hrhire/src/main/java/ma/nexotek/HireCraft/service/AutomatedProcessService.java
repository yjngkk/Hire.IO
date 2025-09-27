package ma.nexotek.HireCraft.service;

import ma.nexotek.HireCraft.dto.Test.SendRandomTestRequest;
import ma.nexotek.HireCraft.dto.Test.TestAssignmentResponse;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.model.CVScoringResult;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.FormRepository;
import ma.nexotek.HireCraft.service.ai.CVScoringService; // NOUVEAU: Service IA
import ma.nexotek.HireCraft.service.ai.CVTextExtractionService; // NOUVEAU
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AutomatedProcessService {

    // NOUVEAU: Services IA au lieu de l'ancien service de simulation
    @Autowired
    private CVScoringService cvScoringService; // Service IA réel

    @Autowired
    private CVTextExtractionService cvTextExtractionService; // Pour vérifier le CV

    @Autowired
    private TestAssignmentService testAssignmentService;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private TimelineService timelineService;

    public void processNewCandidat(Candidat candidat, Long offreId) {
        try {
            log.info("Début traitement automatique candidat: {} pour offre: {}", candidat.getNom(), offreId);

            candidat.setProcessStatus("PENDING");
            candidatRepository.save(candidat);

            // NOUVEAU: Récupérer l'offre pour le scoring IA
            Form offre = formRepository.findById(offreId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable: " + offreId));

            // Associer candidat à l'offre si pas déjà fait
            if (candidat.getOffre() == null) {
                candidat.setOffre(offre);
                candidatRepository.save(candidat);
            }

            // NOUVEAU: Scoring IA réel (au lieu du scoring aléatoire)
            Double cvScore = performAIScoring(candidat, offre);
            
            timelineService.addCvReviewed(candidat);
            candidat.setCvScore(cvScore);
            candidatRepository.save(candidat);

            log.info("Score IA calculé pour {}: {:.1f}%", candidat.getNom(), cvScore);

            // Logique de décision basée sur le score IA
            if (cvScore > 50.0) {
                sendAutomaticTest(candidat, offreId);
            } else {
                candidat.setProcessStatus("REJECTED");
                candidatRepository.save(candidat);
                testAssignmentService.sendRejectionEmail(candidat, cvScore, offreId);
                log.info("Candidat {} rejeté automatiquement (score IA: {:.1f}%)", candidat.getNom(), cvScore);
            }

        } catch (Exception e) {
            candidat.setProcessStatus("ERROR");
            candidatRepository.save(candidat);
            log.error("Erreur lors du traitement automatique candidat {}: {}", candidat.getId(), e.getMessage(), e);
        }
    }

    /**
     * NOUVEAU: Méthode de scoring IA (remplace l'ancien scoring aléatoire)
     */
    private Double performAIScoring(Candidat candidat, Form offre) {
        try {
            // Vérifier que le CV est lisible
            if (!cvTextExtractionService.isValidCV(candidat)) {
                log.warn("CV non valide pour candidat {}", candidat.getId());
                return 0.0; // Score minimum si CV illisible
            }

            // Utiliser le service IA pour calculer le score réel
            CVScoringResult result = cvScoringService.scoreCV(candidat, offre);
            
            // Mettre à jour le statut selon la décision IA
            candidat.setProcessStatus(result.getNewStatus());
            
            log.info("Scoring IA terminé pour candidat {}: {:.1f}% (Décision: {})", 
                    candidat.getId(), result.getFinalScore(), result.getAiDecision());
            
            return result.getFinalScore();

        } catch (Exception e) {
            log.error("Erreur scoring IA candidat {}: {}", candidat.getId(), e.getMessage());
            // En cas d'erreur, retourner un score de sécurité
            return 25.0; // Score bas mais pas zéro pour permettre une révision manuelle
        }
    }

    private void sendAutomaticTest(Candidat candidat, Long offreId) {
        try {
            SendRandomTestRequest request = new SendRandomTestRequest(
                    candidat.getId(),
                    offreId,
                    "Félicitations ! Votre CV a été présélectionné par notre IA. Veuillez passer ce test technique."
            );

            TestAssignmentResponse response = testAssignmentService.sendRandomTestToCandidat(request);

            candidat.setProcessStatus("TEST_SENT");
            candidatRepository.save(candidat);

            log.info("Test envoyé automatiquement à {} (Test: {})", 
                    candidat.getNom(), response.getTestTitle());

        } catch (Exception e) {
            candidat.setProcessStatus("ERROR");
            candidatRepository.save(candidat);
            log.error("Erreur lors de l'envoi automatique du test pour candidat {}: {}", 
                    candidat.getId(), e.getMessage(), e);
        }
    }
}