package ma.nexotek.HireCraft.controller;

import ma.nexotek.HireCraft.dto.JobOfferResponse;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.repository.FormRepository;
import ma.nexotek.HireCraft.service.LinkedInService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/linkedin")
public class LinkedInController {

    private final LinkedInService linkedInService;
    private final FormRepository formRepository;

    public LinkedInController(LinkedInService linkedInService, FormRepository formRepository) {
        this.linkedInService = linkedInService;
        this.formRepository = formRepository;
    }

    @PostMapping("/publish/{formId}")
    public JobOfferResponse publishOfferToLinkedIn(@PathVariable Long formId) {
        long startTime = System.currentTimeMillis();

        Optional<Form> formOpt = formRepository.findById(formId);

        if (formOpt.isEmpty()) {
            return JobOfferResponse.error("Form with ID " + formId + " not found.");
        }

        Form form = formOpt.get();

        if (form.getGeneratedContent() == null || form.getGeneratedContent().isEmpty()) {
            return JobOfferResponse.error("No generated content found in the form.");
        }

        if (form.getPublished() != null && form.getPublished()) {
            return JobOfferResponse.error("This job offer has already been published on LinkedIn.");
        }

        try {
            linkedInService.publishPost(form.getGeneratedContent());

            // Mettre à jour le statut de publication
            form.setPublished(true);
            form.setPublishedAt(LocalDateTime.now());
            formRepository.save(form);

            long duration = System.currentTimeMillis() - startTime;

            return JobOfferResponse.success(
                    formId,
                    form.getGeneratedContent(),
                    "LinkedIn-Publisher-v1",
                    "LinkedIn",
                    duration
            );
        } catch (Exception e) {
            return JobOfferResponse.error("Failed to publish to LinkedIn: " + e.getMessage());
        }
    }

    @DeleteMapping("/unpublish/{formId}")
    public JobOfferResponse unpublishOfferFromLinkedIn(@PathVariable Long formId) {
        Optional<Form> formOpt = formRepository.findById(formId);

        if (formOpt.isEmpty()) {
            return JobOfferResponse.error("Form with ID " + formId + " not found.");
        }

        Form form = formOpt.get();

        if (form.getPublished() == null || !form.getPublished()) {
            return JobOfferResponse.error("This job offer has not been published on LinkedIn.");
        }

        try {
            // Note: LinkedIn API ne permet pas de supprimer des posts directement
            // On peut seulement marquer comme non publié dans notre base de données
            form.setPublished(false);
            form.setPublishedAt(null);
            formRepository.save(form);

            return JobOfferResponse.success(
                    formId,
                    "Job offer unpublished successfully",
                    "LinkedIn-Publisher-v1",
                    "LinkedIn",
                    0
            );
        } catch (Exception e) {
            return JobOfferResponse.error("Failed to unpublish from LinkedIn: " + e.getMessage());
        }
    }
}
