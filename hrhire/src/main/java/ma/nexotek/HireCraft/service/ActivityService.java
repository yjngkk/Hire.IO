package ma.nexotek.HireCraft.service;

import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ActivityService {

    @Autowired
    private CandidatRepository candidatRepository;

    public List<Map<String, Object>> getRecentActivities(int limit) {

        try {
            // Validation des paramètres
            if (limit <= 0) {
                limit = 5;
            }
            if (limit > 50) {
                limit = 50;
            }

            // Récupérer les candidats les plus récents triés par createdAt
            List<Candidat> recentCandidats = candidatRepository.findTopCandidatsOrderByCreatedAtDesc(limit);


            // Filtrer les candidats valides avant conversion
            List<Candidat> validCandidats = recentCandidats.stream()
                    .filter(this::isValidCandidat)
                    .collect(Collectors.toList());

            if (validCandidats.size() != recentCandidats.size()) {
                log.warn("Filtrage: {} candidatures invalides ignorées",
                        recentCandidats.size() - validCandidats.size());
            }

            // Convertir en format d'activité
            List<Map<String, Object>> activities = validCandidats.stream()
                    .map(this::createApplicationActivity)
                    .filter(Objects::nonNull) // Filtrer les activités nulles
                    .collect(Collectors.toList());

            return activities;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getActivitiesFromLastDays(int days, int limit) {

        try {
            // Validation des paramètres
            if (days <= 0 || days > 365) {
                days = 7;
            }
            if (limit <= 0) {
                limit = 10;
            }
            if (limit > 100) {
                limit = 100;
            }

            LocalDateTime daysAgo = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            List<Candidat> recentCandidats = candidatRepository
                    .findByCreatedAtAfterOrderByCreatedAtDesc(daysAgo);


            // Filtrer et limiter
            List<Candidat> validCandidats = recentCandidats.stream()
                    .filter(this::isValidCandidat)
                    .limit(limit)
                    .collect(Collectors.toList());

            List<Map<String, Object>> activities = validCandidats.stream()
                    .map(this::createApplicationActivity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return activities;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public Map<String, Object> getActivityStats() {

        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minus(1, ChronoUnit.DAYS);
            LocalDateTime oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
            LocalDateTime oneMonthAgo = now.minus(30, ChronoUnit.DAYS);

            // Compter les candidatures par période en utilisant createdAt
            long candidaturesToday = candidatRepository
                    .countByCreatedAtAfter(oneDayAgo);
            long candidaturesThisWeek = candidatRepository
                    .countByCreatedAtAfter(oneWeekAgo);
            long candidaturesThisMonth = candidatRepository
                    .countByCreatedAtAfter(oneMonthAgo);
            long totalCandidatures = candidatRepository.count();

            stats.put("candidaturesToday", candidaturesToday);
            stats.put("candidaturesThisWeek", candidaturesThisWeek);
            stats.put("candidaturesThisMonth", candidaturesThisMonth);
            stats.put("totalCandidatures", totalCandidatures);
            stats.put("lastUpdated", LocalDateTime.now());
            return stats;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques d'activité", e);
            // Retourner des statistiques par défaut
            stats.put("candidaturesToday", 0);
            stats.put("candidaturesThisWeek", 0);
            stats.put("candidaturesThisMonth", 0);
            stats.put("totalCandidatures", 0);
            stats.put("lastUpdated", LocalDateTime.now());
            stats.put("error", "Erreur lors du calcul");
            return stats;
        }
    }


    private Map<String, Object> createApplicationActivity(Candidat candidat) {
        try {
            if (candidat == null) {
                return null;
            }

            Map<String, Object> activity = new HashMap<>();

            // Champs obligatoires
            activity.put("id", candidat.getId());
            activity.put("type", "application");
            activity.put("candidate", candidat.getNom() != null ? candidat.getNom() : "Nom inconnu");
            activity.put("poste", candidat.getPoste() != null ? candidat.getPoste() : "Poste non spécifié");
            activity.put("message", "Nouvelle candidature pour " + activity.get("poste"));
            activity.put("timestamp", candidat.getCreatedAt());
            activity.put("time", formatTimeAgo(candidat.getCreatedAt()));
            activity.put("status", determineStatus(candidat));

            // Champs optionnels
            activity.put("email", candidat.getEmail() != null ? candidat.getEmail() : "");
            activity.put("telephone", candidat.getTelephone() != null ? candidat.getTelephone() : "");

            // Ajouter l'offre si disponible
            if (candidat.getOffre() != null) {
                activity.put("offreId", candidat.getOffre().getId());
                activity.put("offreTitre", candidat.getPoste() != null ?
                        candidat.getPoste() : candidat.getPoste());
            }

            return activity;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Valide qu'un candidat a les champs requis pour créer une activité
     */
    private boolean isValidCandidat(Candidat candidat) {
        if (candidat == null) {
            return false;
        }

        // Vérifier les champs essentiels
        if (candidat.getId() == null) {
            return false;
        }

        if (candidat.getCreatedAt() == null) {
            return false;
        }

        if (candidat.getNom() == null || candidat.getNom().trim().isEmpty()) {
            return false;
        }

        return true;
    }


    private String determineStatus(Candidat candidat) {
        if (candidat == null || candidat.getProcessStatus() == null ||
                candidat.getProcessStatus().trim().isEmpty()) {
            return "nouveau";
        }

        try {
            switch (candidat.getProcessStatus().toUpperCase().trim()) {
                case "CREATED":
                case "NEW":
                    return "nouveau";
                case "IN_PROGRESS":
                case "PROGRESS":
                    return "en cours";
                case "INTERVIEWED":
                case "INTERVIEW":
                    return "entretien passé";
                case "ACCEPTED":
                case "ACCEPT":
                    return "accepté";
                case "REJECTED":
                case "REJECT":
                    return "rejeté";
                default:

                    return "nouveau";
            }
        } catch (Exception e) {

            return "nouveau";
        }
    }


    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Date inconnue";
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(dateTime, now);

            if (minutes < 0) {
                return "À l'instant";
            } else if (minutes < 1) {
                return "À l'instant";
            } else if (minutes < 60) {
                return "Il y a " + minutes + "min";
            } else if (minutes < 1440) { // moins de 24h
                long hours = minutes / 60;
                return "Il y a " + hours + "h";
            } else {
                long days = minutes / 1440;
                if (days == 1) {
                    return "Hier";
                } else if (days <= 7) {
                    return "Il y a " + days + " jours";
                } else if (days <= 30) {
                    return "Il y a " + days + " jours";
                } else {
                    // Pour les dates très anciennes, afficher la date
                    return dateTime.toLocalDate().toString();
                }
            }
        } catch (Exception e) {
            return "Date invalide";
        }
    }
}