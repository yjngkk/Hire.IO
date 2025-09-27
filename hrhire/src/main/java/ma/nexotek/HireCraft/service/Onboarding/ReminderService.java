package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.CandidateFile;
import ma.nexotek.HireCraft.service.Onboarding.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    @Autowired
    private EmailSenderService emailSenderService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Send file reminder email to candidate
     */
    public void sendFileReminderEmail(Candidat candidat, List<CandidateFile> filesToRemind) {
        try {
            logger.info("Preparing reminder email for candidate: {} with {} files",
                    candidat.getEmail(), filesToRemind.size());

            String subject = "Rappel - Documents manquants pour votre intégration";
            String htmlContent = buildReminderEmailContent(candidat, filesToRemind);

            emailSenderService.sendHtmlEmail(candidat.getEmail(), subject, htmlContent);

            logger.info("Reminder email sent successfully to candidate: {}", candidat.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send reminder email to candidate {}: {}",
                    candidat.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de rappel: " + e.getMessage(), e);
        }
    }

    /**
     * Send welcome email to new candidate
     */
    public void sendWelcomeEmail(Candidat candidat, String onboardingPlanDetails) {
        try {
            String subject = "Bienvenue dans l'équipe - Prochaines étapes";
            String htmlContent = buildWelcomeEmailContent(candidat, onboardingPlanDetails);

            emailSenderService.sendHtmlEmail(candidat.getEmail(), subject, htmlContent);

            logger.info("Welcome email sent to: {}", candidat.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}",
                    candidat.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTML content for file reminder email - PROFESSIONAL STYLE
     */
    private String buildReminderEmailContent(Candidat candidat, List<CandidateFile> filesToRemind) {
        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Rappel Documents</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #2c3e50; margin: 0; padding: 0; background-color: #f8f9fa; }")
                .append(".email-container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-align: center; padding: 40px 20px; }")
                .append(".header h1 { margin: 0; font-size: 32px; font-weight: 300; letter-spacing: 2px; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; font-weight: 300; }")
                .append(".content { padding: 40px; }")
                .append(".greeting { font-size: 18px; margin-bottom: 25px; }")
                .append(".intro-text { font-size: 16px; line-height: 1.7; margin-bottom: 30px; color: #34495e; }")
                .append(".documents-section { background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin: 25px 0; border-left: 4px solid #667eea; }")
                .append(".documents-title { font-size: 18px; font-weight: 600; color: #2c3e50; margin: 0 0 20px 0; }")
                .append(".document-list { list-style: none; padding: 0; margin: 0; }")
                .append(".document-item { background: white; margin-bottom: 15px; padding: 20px; border-radius: 6px; border-left: 3px solid #3498db; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }")
                .append(".document-name { font-weight: 600; font-size: 16px; color: #2c3e50; margin-bottom: 8px; }")
                .append(".document-description { color: #7f8c8d; font-style: italic; margin-bottom: 5px; line-height: 1.5; }")
                .append(".document-formats { color: #95a5a6; font-size: 13px; }")
                .append(".action-text { font-size: 16px; line-height: 1.7; margin: 30px 0; color: #34495e; }")
                .append(".thank-you { font-size: 16px; color: #34495e; margin: 25px 0; }")
                .append(".signature { margin-top: 40px; }")
                .append(".signature-line { font-size: 16px; margin-bottom: 5px; }")
                .append(".signature-title { font-weight: 600; color: #2c3e50; }")
                .append(".footer { background-color: #ecf0f1; padding: 20px; text-align: center; border-top: 1px solid #bdc3c7; }")
                .append(".footer-text { font-size: 12px; color: #7f8c8d; margin: 0; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='email-container'>")

                // Header
                .append("<div class='header'>")
                .append("<h1>HireCraft</h1>")
                .append("<p>Département des Ressources Humaines</p>")
                .append("</div>")

                // Main content
                .append("<div class='content'>")
                .append("<div class='greeting'>Bonjour <strong>").append(candidat.getNom()).append("</strong>,</div>")

                .append("<div class='intro-text'>")
                .append("Nous vous prions de bien vouloir noter que votre dossier d'intégration nécessite la transmission de certains documents complémentaires afin de finaliser le processus administratif.")
                .append("</div>")

                .append("<div class='documents-section'>")
                .append("<div class='documents-title'>Documents requis</div>")
                .append("<ul class='document-list'>");

        for (CandidateFile file : filesToRemind) {
            content.append("<li class='document-item'>")
                    .append("<div class='document-name'>").append(file.getName()).append("</div>");

            if (file.getDescription() != null && !file.getDescription().isEmpty()) {
                content.append("<div class='document-description'>").append(file.getDescription()).append("</div>");
            }

            if (file.getAcceptedFormats() != null && !file.getAcceptedFormats().isEmpty()) {
                content.append("<div class='document-formats'>Formats acceptés : ").append(file.getAcceptedFormats()).append("</div>");
            }
            content.append("</li>");
        }

        content.append("</ul>")
                .append("</div>")

                .append("<div class='action-text'>")
                .append("Nous vous prions de bien vouloir nous transmettre ces documents dans les meilleurs délais. Pour toute question, vous pouvez contacter notre département des ressources humaines.")
                .append("</div>")

                .append("<div class='thank-you'>")
                .append("Nous vous remercions par avance pour votre collaboration.")
                .append("</div>")

                .append("<div class='signature'>")
                .append("<div class='signature-line'>Cordialement,</div>")
                .append("<div class='signature-line signature-title'>Le Département des Ressources Humaines</div>")
                .append("<div class='signature-line'>HireCraft</div>")
                .append("</div>")

                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("<p class='footer-text'>Ce courrier électronique a été généré automatiquement.</p>")
                .append("</div>")

                .append("</div>")
                .append("</body>")
                .append("</html>");

        return content.toString();
    }

    /**
     * Build HTML content for welcome email - PROFESSIONAL STYLE
     */
    private String buildWelcomeEmailContent(Candidat candidat, String planDetails) {
        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Bienvenue</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #2c3e50; margin: 0; padding: 0; background-color: #f8f9fa; }")
                .append(".email-container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%); color: white; text-align: center; padding: 40px 20px; }")
                .append(".header h1 { margin: 0; font-size: 32px; font-weight: 300; letter-spacing: 2px; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; font-weight: 300; }")
                .append(".content { padding: 40px; }")
                .append(".greeting { font-size: 18px; margin-bottom: 25px; }")
                .append(".welcome-text { font-size: 16px; line-height: 1.7; margin-bottom: 30px; color: #34495e; }")
                .append(".plan-section { background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin: 25px 0; border-left: 4px solid #27ae60; }")
                .append(".plan-title { font-weight: 600; font-size: 16px; color: #2c3e50; margin-bottom: 10px; }")
                .append(".plan-details { color: #34495e; line-height: 1.6; }")
                .append(".closing-text { font-size: 16px; color: #34495e; margin: 25px 0; }")
                .append(".signature { margin-top: 40px; }")
                .append(".signature-line { font-size: 16px; margin-bottom: 5px; }")
                .append(".signature-title { font-weight: 600; color: #2c3e50; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='email-container'>")

                .append("<div class='header'>")
                .append("<h1>Bienvenue dans notre équipe</h1>")
                .append("<p>Département des Ressources Humaines</p>")
                .append("</div>")

                .append("<div class='content'>")
                .append("<div class='greeting'>Bonjour <strong>").append(candidat.getNom()).append("</strong>,</div>")
                .append("<div class='welcome-text'>Nous avons le plaisir de vous accueillir officiellement au sein de notre organisation.</div>")

                .append("<div class='plan-section'>")
                .append("<div class='plan-title'>Prochaines étapes</div>")
                .append("<div class='plan-details'>")
                .append(planDetails != null ? planDetails : "Les détails concernant votre plan d'intégration vous seront communiqués prochainement.")
                .append("</div>")
                .append("</div>")

                .append("<div class='closing-text'>Nous vous souhaitons une excellente intégration.</div>")

                .append("<div class='signature'>")
                .append("<div class='signature-line'>Cordialement,</div>")
                .append("<div class='signature-line signature-title'>Le Département des Ressources Humaines</div>")
                .append("<div class='signature-line'>HireCraft</div>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return content.toString();
    }

    /**
     * Send reminder email for a single file
     */
    public void sendIndividualFileReminder(Candidat candidat, CandidateFile file) {
        try {
            logger.info("Preparing individual file reminder for candidate: {} for file: {}",
                    candidat.getEmail(), file.getName());

            String subject = "Rappel - Document requis : " + file.getName();
            String htmlContent = buildIndividualFileReminderContent(candidat, file);

            emailSenderService.sendHtmlEmail(candidat.getEmail(), subject, htmlContent);

            logger.info("Individual file reminder sent successfully to candidate: {} for file: {}",
                    candidat.getEmail(), file.getName());

        } catch (Exception e) {
            logger.error("Failed to send individual file reminder to candidate {} for file {}: {}",
                    candidat.getEmail(), file.getName(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi du rappel: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTML content for individual file reminder email - PROFESSIONAL STYLE
     */
    private String buildIndividualFileReminderContent(Candidat candidat, CandidateFile file) {
        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Rappel Document</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #2c3e50; margin: 0; padding: 0; background-color: #f8f9fa; }")
                .append(".email-container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-align: center; padding: 40px 20px; }")
                .append(".header h1 { margin: 0; font-size: 32px; font-weight: 300; letter-spacing: 2px; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; font-weight: 300; }")
                .append(".content { padding: 40px; }")
                .append(".greeting { font-size: 18px; margin-bottom: 25px; }")
                .append(".intro-text { font-size: 16px; line-height: 1.7; margin-bottom: 30px; color: #34495e; }")
                .append(".document-section { background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin: 25px 0; border-left: 4px solid #e74c3c; }")
                .append(".document-title { font-size: 18px; font-weight: 600; color: #2c3e50; margin: 0 0 15px 0; }")
                .append(".document-card { background: white; padding: 20px; border-radius: 6px; border-left: 3px solid #e74c3c; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }")
                .append(".document-name { font-weight: 600; font-size: 16px; color: #2c3e50; margin-bottom: 8px; }")
                .append(".document-description { color: #7f8c8d; font-style: italic; margin-bottom: 5px; line-height: 1.5; }")
                .append(".document-formats { color: #95a5a6; font-size: 13px; }")
                .append(".action-text { font-size: 16px; line-height: 1.7; margin: 30px 0; color: #34495e; }")
                .append(".thank-you { font-size: 16px; color: #34495e; margin: 25px 0; }")
                .append(".signature { margin-top: 40px; }")
                .append(".signature-line { font-size: 16px; margin-bottom: 5px; }")
                .append(".signature-title { font-weight: 600; color: #2c3e50; }")
                .append(".footer { background-color: #ecf0f1; padding: 20px; text-align: center; border-top: 1px solid #bdc3c7; }")
                .append(".footer-text { font-size: 12px; color: #7f8c8d; margin: 0; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='email-container'>")

                // Header
                .append("<div class='header'>")
                .append("<h1>HireCraft</h1>")
                .append("<p>Département des Ressources Humaines</p>")
                .append("</div>")

                // Main content
                .append("<div class='content'>")
                .append("<div class='greeting'>Bonjour <strong>").append(candidat.getNom()).append("</strong>,</div>")

                .append("<div class='intro-text'>")
                .append("Nous vous prions de bien vouloir noter qu'un document complémentaire est requis pour finaliser votre dossier d'intégration.")
                .append("</div>")

                // File details
                .append("<div class='document-section'>")
                .append("<div class='document-title'>Document requis</div>")
                .append("<div class='document-card'>")
                .append("<div class='document-name'>").append(file.getName()).append("</div>");

        if (file.getDescription() != null && !file.getDescription().isEmpty()) {
            content.append("<div class='document-description'>").append(file.getDescription()).append("</div>");
        }

        if (file.getAcceptedFormats() != null && !file.getAcceptedFormats().isEmpty()) {
            content.append("<div class='document-formats'>Formats acceptés : ").append(file.getAcceptedFormats()).append("</div>");
        }

        content.append("</div>")
                .append("</div>")

                .append("<div class='action-text'>")
                .append("Nous vous prions de bien vouloir nous transmettre ce document dans les meilleurs délais. Pour toute question, vous pouvez contacter notre département des ressources humaines.")
                .append("</div>")

                .append("<div class='thank-you'>")
                .append("Nous vous remercions par avance pour votre collaboration.")
                .append("</div>")

                .append("<div class='signature'>")
                .append("<div class='signature-line'>Cordialement,</div>")
                .append("<div class='signature-line signature-title'>Le Département des Ressources Humaines</div>")
                .append("<div class='signature-line'>HireCraft</div>")
                .append("</div>")

                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("<p class='footer-text'>Ce courrier électronique a été généré automatiquement.</p>")
                .append("</div>")

                .append("</div>")
                .append("</body>")
                .append("</html>");

        return content.toString();
    }
}