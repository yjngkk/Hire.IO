package ma.nexotek.HireCraft.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.InputStreamReader;
import java.util.Arrays;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Service-Calendrier";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;
    
    private final GoogleAuthorizationCodeFlow flow;

    public String createMeetEvent(String summary, String description, LocalDateTime start, LocalDateTime end, String organizerEmail, String interviewerEmail, String candidateEmail) throws GeneralSecurityException, IOException {
        Calendar service = getCalendarService();
        Event event = new Event()
                .setSummary(summary)
                .setDescription(description);
        // Set location to Meet link only (no phone)
        event.setLocation(""); // Or set to meetLink if you want the Meet link in location

        // Utiliser la timezone du Maroc pour la conversion
        java.time.ZoneId zoneId = java.time.ZoneId.of("Africa/Casablanca");
        java.util.Date startDate = java.util.Date.from(start.atZone(zoneId).toInstant());
        java.util.Date endDate = java.util.Date.from(start.plusHours(1).atZone(zoneId).toInstant());
        EventDateTime startEventDateTime = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDate));
        EventDateTime endEventDateTime = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDate));
        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);

        // Set organizer
        event.setOrganizer(new Event.Organizer().setEmail(organizerEmail));
        // Add only the specified emails as attendees (no duplicates)
        java.util.List<EventAttendee> eventAttendees = new java.util.ArrayList<>();
        eventAttendees.add(new EventAttendee().setEmail(organizerEmail));
        if (!organizerEmail.equalsIgnoreCase(interviewerEmail)) {
            eventAttendees.add(new EventAttendee().setEmail(interviewerEmail));
        }
        if (!organizerEmail.equalsIgnoreCase(candidateEmail) && !interviewerEmail.equalsIgnoreCase(candidateEmail)) {
            eventAttendees.add(new EventAttendee().setEmail(candidateEmail));
        }
        event.setAttendees(eventAttendees);

        // Add Google Meet conference
        ConferenceData conferenceData = new ConferenceData();
        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
        createConferenceRequest.setRequestId("meet-" + System.currentTimeMillis());
        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
        conferenceSolutionKey.setType("hangoutsMeet");
        createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);
        conferenceData.setCreateRequest(createConferenceRequest);
        event.setConferenceData(conferenceData);

        try {
            Calendar.Events.Insert request = service.events().insert("primary", event);
            request.setConferenceDataVersion(1);
            request.setSendUpdates("all"); // Google envoie l'invitation native à tous les invités
            event = request.execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            System.err.println("[GOOGLE CALENDAR ERROR] " + e.getDetails());
            if (e.getDetails() != null && e.getDetails().getMessage() != null && e.getDetails().getMessage().contains("Invalid conference type value")) {
                throw new IOException("Erreur Google Meet : Le compte Google utilisé doit être un compte Google Workspace (ex-GSuite) pour générer un lien Meet par API. Détail : " + e.getDetails().getMessage());
            }
            throw e;
        }

        // Get Meet link
        if (event.getConferenceData() != null && event.getConferenceData().getEntryPoints() != null) {
            for (EntryPoint entryPoint : event.getConferenceData().getEntryPoints()) {
                if ("video".equals(entryPoint.getEntryPointType())) {
                    return entryPoint.getUri();
                }
            }
        }
        return null;
    }

    public GoogleCalendarService(GoogleAuthorizationCodeFlow flow) {
        this.flow = flow;
    }

    private Calendar getCalendarService() throws GeneralSecurityException, IOException {
        Credential credential = flow.loadCredential("user");
        if (credential == null || credential.getAccessToken() == null) {
            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(CALLBACK_URI)
                    .build();
            throw new IOException("Please authorize the application by visiting: " + authorizationUrl);
        }

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
} 