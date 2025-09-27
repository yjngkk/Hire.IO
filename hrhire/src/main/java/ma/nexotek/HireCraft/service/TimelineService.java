package ma.nexotek.HireCraft.service;

import ma.nexotek.HireCraft.model.*;
import ma.nexotek.HireCraft.enums.*;
import ma.nexotek.HireCraft.repository.TimelineEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimelineService {

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    public void createTimelineEvent(Candidat candidat, String eventType, String title, String description, EventStatus status) {
        TimelineEvent event = TimelineEvent.builder()
                .candidat(candidat)
                .eventType(eventType)
                .title(title)
                .description(description)
                .eventDate(LocalDateTime.now())
                .status(status)
                .iconType(getIconForEventType(eventType))
                .build();

        timelineEventRepository.save(event);
    }
    private String getIconForEventType(String eventType) {
        return switch (eventType) {
            case "CANDIDATURE_RECEIVED" -> "document";
            case "CV_REVIEWED" -> "user";
            case "EMAIL_SENT" -> "mail";
            case "INTERVIEW_SCHEDULED" -> "phone";
            case "TECHNICAL_INTERVIEW" -> "video";
            default -> "circle";
        };
    }


    public List<TimelineEvent> getCandidateTimeline(Long candidatId) {
        return timelineEventRepository.findByCandidatIdOrderByEventDateDesc(candidatId);
    }


    // Méthodes utilitaires pour créer des événements spécifiques
    public void addCandidatureReceived(Candidat candidat) {
        createTimelineEvent(candidat, "CANDIDATURE_RECEIVED", "Candidature reçue",
                "Le candidat a postulé pour le poste de " + candidat.getPoste(), EventStatus.COMPLETED);
    }
    public void addEntretien(Candidat candidat) {
        createTimelineEvent(candidat, "INTERVIEW_SCHEDULED","Entretien programmé",
                "Entretien programmé pour le candidat " + candidat.getNom()
              ,EventStatus.SCHEDULED);
    }
    public void addCvReviewed(Candidat candidat) {
        createTimelineEvent(candidat, "CV_REVIEWED", "CV examiné",
                "Le CV a été examiné par l'équipe RH", EventStatus.COMPLETED);
    }

    public void addEmailSent(Candidat candidat) {
        createTimelineEvent(candidat, "EMAIL_SENT", "Email de confirmation envoyé",
                "Email de confirmation de réception de candidature", EventStatus.COMPLETED);
    }

    public void schedulePhoneInterview(Candidat candidat, LocalDateTime interviewDate) {
        TimelineEvent event = TimelineEvent.builder()
                .candidat(candidat)
                .eventType("INTERVIEW_SCHEDULED")
                .title("Entretien téléphonique planifié")
                .description("Entretien prévu le " + interviewDate.toString())
                .eventDate(interviewDate)
                .status(EventStatus.SCHEDULED)
                .build();

        timelineEventRepository.save(event);
    }

    public void scheduleTechnicalInterview(Candidat candidat, LocalDateTime interviewDate) {
        TimelineEvent event = TimelineEvent.builder()
                .candidat(candidat)
                .eventType("TECHNICAL_INTERVIEW")
                .title("Entretien technique à venir")
                .description("Entretien technique avec l'équipe dev")
                .eventDate(interviewDate)
                .status(EventStatus.PENDING)
                .build();

        timelineEventRepository.save(event);
    }
}
