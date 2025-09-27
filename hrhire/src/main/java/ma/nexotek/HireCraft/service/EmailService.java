package ma.nexotek.HireCraft.service;

import ma.nexotek.HireCraft.dto.MeetingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class EmailService {
    public static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Templates d'emails


    @Value("${email.reminder.subject:🔔 RAPPEL : Entretien dans moins d'une heure - }")
    private String reminderSubject;

    @Value("${email.reminder.default-position:Technique}")
    private String defaultPosition;

    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }


    public void sendMeetingEmails(String organizerEmail, String interviewerEmail, String candidateEmail,
                                  LocalDateTime dateTime, String meetLink) throws MessagingException {
        String subject = "Entretien - Développeur Java - " + candidateEmail;
        sendCalendarOnly(organizerEmail, subject, dateTime, meetLink, candidateEmail, interviewerEmail, organizerEmail);
        sendCustomTextEmail(interviewerEmail, subject, dateTime, meetLink, "Développeur Java", interviewerEmail, organizerEmail);
        sendCustomTextEmail(candidateEmail, subject, dateTime, meetLink, "Développeur Java", interviewerEmail, organizerEmail);
    }

    public void sendInterviewInvitation(
            String position,
            LocalDateTime dateTime,
            String interviewerName,
            String interviewerEmail,
            String candidateName,
            String candidateEmail,
            String meetLink,
            String organizerEmail
    ) throws MessagingException, IOException {
    String pos = (position != null && !position.isEmpty()) ? position : defaultPosition;
    String subject = "Invitation à un entretien pour le poste de " + pos;
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));

    String calendarContent = buildCalendarContent(pos, dateTime, meetLink, organizerEmail, candidateEmail, interviewerEmail);

    // Préparer le contexte commun
    Context context = new Context();
    context.setVariable("dateTime", formattedDate);
    context.setVariable("position", pos);
    context.setVariable("interviewerName", interviewerName);
    context.setVariable("interviewerEmail", interviewerEmail);
    context.setVariable("candidateName", candidateName);
    context.setVariable("candidateEmail", candidateEmail);
    context.setVariable("meetLink", meetLink);

    // Email à l'organisateur
    String organizerBody = templateEngine.process("organizer-email.html", context);
    sendEmailWithIcs(organizerEmail, subject, organizerBody, calendarContent);

    // Email au candidat
    String candidateBody = templateEngine.process("candidate-email.html", context);
    sendEmailWithIcs(candidateEmail, subject, candidateBody, calendarContent);

    // Email à l'intervieweur
    String interviewerBody = templateEngine.process("interviewer-email.html", context);
    sendEmailWithIcs(interviewerEmail, subject, interviewerBody, calendarContent);
    }

    // Nouvelle méthode pour les rappels d'entretien
    public void sendInterviewReminder(
            String position,
            LocalDateTime dateTime,
            String interviewerName,
            String interviewerEmail,
            String candidateName,
            String candidateEmail,
            String meetLink,
            String organizerEmail
    ) throws MessagingException, IOException {
    String pos = (position != null && !position.isEmpty()) ? position : defaultPosition;
    String subject = new String((reminderSubject + pos).getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));

    String calendarContent = buildCalendarContent(pos, dateTime, meetLink, organizerEmail, candidateEmail, interviewerEmail);

    // Préparer le contexte pour le template
    Context context = new Context();
    context.setVariable("dateTime", formattedDate);
    context.setVariable("position", pos);
    context.setVariable("interviewerName", interviewerName);
    context.setVariable("candidateName", candidateName);
    context.setVariable("meetLink", meetLink);

    // Email de rappel au candidat
    context.setVariable("recipientName", candidateName);
    String candidateBody = templateEngine.process("reminder-email.html", context);
    sendEmailWithIcs(candidateEmail, subject, candidateBody, calendarContent);

    // Email de rappel à l'intervieweur
    context.setVariable("recipientName", interviewerName);
    String interviewerBody = templateEngine.process("reminder-email.html", context);
    sendEmailWithIcs(interviewerEmail, subject, interviewerBody, calendarContent);

    // Email de rappel à l'organisateur
    context.setVariable("recipientName", interviewerName);
    String organizerBody = templateEngine.process("reminder-email.html", context);
    sendEmailWithIcs(organizerEmail, subject, organizerBody, calendarContent);
    }

    // Nouvelle méthode pour les notifications de statut d'entretien
    public void sendInterviewStatusNotification(
            String candidateEmail,
            String interviewerEmail,
            String organizerEmail,
            String statusTitle,
            String statusMessage,
            String position,
            LocalDateTime dateTime
    ) throws MessagingException, IOException {
    String pos = (position != null && !position.isEmpty()) ? position : defaultPosition;
    String subject = "📢 " + statusTitle + " - " + pos;
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));

    Context context = new Context();
    context.setVariable("statusTitle", statusTitle);
    context.setVariable("dateTime", formattedDate);
    context.setVariable("position", pos);
    context.setVariable("statusMessage", statusMessage);
    String body = templateEngine.process("status-notification-email.html", context);

    // Envoyer à tous les participants
    sendSimpleEmail(candidateEmail, subject, body);
    sendSimpleEmail(interviewerEmail, subject, body);
    sendSimpleEmail(organizerEmail, subject, body);
    }

    // Nouvelle méthode pour les rappels de meeting
    public void sendMeetingReminder(
            String position,
            LocalDateTime dateTime,
            String interviewerName,
            String interviewerEmail,
            String candidateName,
            String candidateEmail,
            String meetLink,
            String organizerEmail
    ) throws MessagingException, IOException {
    String pos = (position != null && !position.isEmpty()) ? position : defaultPosition;
    String subject = new String(("🔔 RAPPEL : Meeting dans moins d'une heure - " + pos).getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));

    Context context = new Context();
    context.setVariable("dateTime", formattedDate);
    context.setVariable("position", pos);
    context.setVariable("interviewerName", interviewerName);
    context.setVariable("candidateName", candidateName);
    context.setVariable("meetLink", meetLink);

    // Email au candidat
    context.setVariable("recipientName", candidateName);
    String candidateBody = templateEngine.process("reminder-email.html", context);
    sendSimpleEmail(candidateEmail, subject, candidateBody);

    // Email à l'intervieweur
    context.setVariable("recipientName", interviewerName);
    String interviewerBody = templateEngine.process("reminder-email.html", context);
    sendSimpleEmail(interviewerEmail, subject, interviewerBody);

    // Email à l'organisateur
    context.setVariable("recipientName", interviewerName);
    String organizerBody = templateEngine.process("reminder-email.html", context);
    sendSimpleEmail(organizerEmail, subject, organizerBody);
    }

    // Nouvelle méthode pour les notifications de statut de meeting
    public void sendMeetingStatusNotification(
            String candidateEmail,
            String interviewerEmail,
            String organizerEmail,
            String statusTitle,
            String statusMessage,
            String position,
            LocalDateTime dateTime
    ) throws MessagingException, IOException {
    String pos = (position != null && !position.isEmpty()) ? position : defaultPosition;
    String subject = "📢 " + statusTitle + " - " + pos;
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));

    Context context = new Context();
    context.setVariable("statusTitle", statusTitle);
    context.setVariable("dateTime", formattedDate);
    context.setVariable("position", pos);
    context.setVariable("statusMessage", statusMessage);
    String body = templateEngine.process("status-notification-email.html", context);

    // Envoyer à tous les participants
    sendSimpleEmail(candidateEmail, subject, body);
    sendSimpleEmail(interviewerEmail, subject, body);
    sendSimpleEmail(organizerEmail, subject, body);
    }

    public void sendDynamicMeetingEmail(MeetingDto dto) {
        String subject = "Interview Invitation";
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(dto.getCandidateEmail()).append(",\n\n");
        body.append("You are invited to an interview");
        if (dto.getPosition() != null && !dto.getPosition().isEmpty()) {
            body.append(" for the position of ").append(dto.getPosition());
        }
        body.append(".\n\n");
        body.append("Date and Time: ").append(dto.getDateTime()).append("\n");
        body.append("Organizer: ").append(dto.getOrganizerEmail()).append("\n");
        body.append("Interviewer: ").append(dto.getInterviewerEmail()).append("\n");
        if (dto.getMeetingLink() != null && !dto.getMeetingLink().isEmpty()) {
            body.append("Meeting Link: ").append(dto.getMeetingLink()).append("\n");
        }
        body.append("\nBest regards,\nRecruitment Team");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(dto.getCandidateEmail());
            helper.setSubject(subject);
            helper.setText(body.toString(), true);
            mailSender.send(message);
            logger.info("Dynamic meeting email sent successfully to: {}", dto.getCandidateEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send dynamic meeting email to: {} - Error: {}", dto.getCandidateEmail(), e.getMessage());
        }
    }

    public void sendZoomMeetingEmail(String candidateName, String candidateEmail, String position, LocalDateTime dateTime, String meetLink) throws MessagingException {
        String subject = "Invitation à un appel Zoom - " + position;
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH));
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("position", position);
        context.setVariable("dateTime", formattedDate);
        context.setVariable("meetLink", meetLink);
        String body = templateEngine.process("zoom-email.html", context);
        sendEmail(candidateEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    private String buildCalendarContent(String position, LocalDateTime dateTime, String meetLink,
                                        String organizerEmail, String candidateEmail, String interviewerEmail) {
        return "BEGIN:VCALENDAR\n" +
                "METHOD:REQUEST\n" +
                "PRODID: Entretien RH\n" +
                "VERSION:2.0\n" +
                "BEGIN:VEVENT\n" +
                "DTSTART:" + dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) + "\n" +
                "DTEND:" + dateTime.plusHours(1).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) + "\n" +
                "SUMMARY:Entretien - " + position + "\n" +
                "DESCRIPTION:Entretien Google Meet\n" +
                "LOCATION:" + meetLink + "\n" +
                "ORGANIZER;CN=RH:mailto:" + organizerEmail + "\n" +
                "ATTENDEE;CN=" + candidateEmail + ":mailto:" + candidateEmail + "\n" +
                "ATTENDEE;CN=" + interviewerEmail + ":mailto:" + interviewerEmail + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";
    }

    private void sendCalendarOnly(String to, String subject, LocalDateTime dateTime, String meetLink,
                                  String candidateEmail, String interviewerEmail, String organizerEmail) throws MessagingException {
        try {
            logger.debug("Preparing to send calendar-only email to: {}", to);
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Email recipient cannot be empty");
            }

            String calendarContent = buildCalendarContent("Développeur Java", dateTime, meetLink,
                    organizerEmail, candidateEmail, interviewerEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText("", false);
            helper.addAttachment("invite.ics", new ByteArrayDataSource(calendarContent, "text/calendar;method=REQUEST;charset=UTF-8"));

            mailSender.send(message);
            logger.info("Calendar-only email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send calendar-only email to: {} - Error: {}", to, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error sending calendar-only email to: {} - Error: {}", to, e.getMessage());
            throw new MessagingException("Unexpected error sending email to: " + to, e);
        }
    }

    private void sendCustomTextEmail(String to, String subject, LocalDateTime dateTime,
                                     String meetLink, String position, String interviewerEmail,
                                     String organizerEmail) throws MessagingException {
        try {
            logger.debug("Preparing to send custom text email to: {}", to);
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Email recipient cannot be empty");
            }

            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE dd MMM yyyy HH:mm", Locale.FRENCH));
            String body = String.format(
                    "Bonjour,\n\n" +
                            "Vous êtes invité(e) à un entretien pour le poste de %s.\n\n" +
                            "📅 Détails de l'entretien :\n" +
                            "- Date et heure : %s\n" +
                            "- Position : %s\n" +
                            "- Intervieweur : %s\n\n" +
                            "🔗 Lien Google Meet pour rejoindre l'entretien :\n%s\n\n" +
                            "⏰ Veuillez vous connecter 5 minutes avant l'heure prévue.\n\n" +
                            "Cordialement,\n",
                    position, formattedDate, position, interviewerEmail, meetLink
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Custom text email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send custom text email to: {} - Error: {}", to, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error sending custom text email to: {} - Error: {}", to, e.getMessage());
            throw new MessagingException("Unexpected error sending email to: " + to, e);
        }
    }

    // Nouvelle méthode pour envoyer des emails simples sans pièce jointe
    private void sendSimpleEmail(String to, String subject, String body) throws MessagingException {
        try {
            logger.debug("Preparing to send simple email to: {}", to);
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Email recipient cannot be empty");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send simple email to: {} - Error: {}", to, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error sending simple email to: {} - Error: {}", to, e.getMessage());
            throw new MessagingException("Unexpected error sending email to: " + to, e);
        }
    }

    private void sendEmailWithIcs(String to, String subject, String body, String calendarContent)
            throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
    helper.setText(body, true);
        helper.addAttachment("entretien.ics", new ByteArrayDataSource(calendarContent, "text/calendar;method=REQUEST;charset=UTF-8"));
        mailSender.send(message);
    }

}