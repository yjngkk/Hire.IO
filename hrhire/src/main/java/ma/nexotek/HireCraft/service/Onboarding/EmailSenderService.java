package ma.nexotek.HireCraft.service.Onboarding;

import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Getter
    @Value("${app.email.from:${spring.mail.username}}")
    private String fromEmail;

    /**
     * Send HTML email - shared utility method
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            logger.info("Attempting to send HTML email to: {}", to);

            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Email recipient cannot be empty");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Send plain text email - shared utility method
     */
    public void sendTextEmail(String to, String subject, String textContent) {
        try {
            logger.info("Attempting to send text email to: {}", to);

            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Email recipient cannot be empty");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false);

            mailSender.send(message);
            logger.info("Text email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send text email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email texte: " + e.getMessage(), e);
        }
    }

}