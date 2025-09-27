package ma.nexotek.HireCraft.service;

import lombok.RequiredArgsConstructor;
import ma.nexotek.HireCraft.dto.FormRequestDTO;
import ma.nexotek.HireCraft.mapper.FormMapper;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.repository.FormRepository;
import ma.nexotek.HireCraft.service.ContentGenerator.ContentGeneratorService;
import ma.nexotek.HireCraft.util.ApplicationLinkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormMapper formMapper;
    @Autowired
    private ApplicationLinkUtil applicationLinkUtil;

    @Autowired
    private ContentGeneratorService contentGeneratorService;

    public Form createForm(Form form) {
        Form savedForm = formRepository.save(form);
        String applicationLink = applicationLinkUtil.generateApplicationLink(savedForm.getId());
        String generatedContent = contentGeneratorService.generateJobContent(
                form.getTitle(),
                form.getSkills(),
                form.getMissions(),
                form.getLocation(),
                form.getContractType(),
                form.getLevel(),
                form.getTone(),
                applicationLink
        );
        savedForm.setGeneratedContent(generatedContent);
        savedForm.setApplicationLink(applicationLink);
        return formRepository.save(savedForm);
    }

    public List<Form> getAllForms() {
        return formRepository.findAllByOrderByCreatedAtDesc();
    }

    public Form getFormById(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found"));
    }

    public Form updateForm(Long id, FormRequestDTO formRequestDTO) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        formMapper.updateEntityFromDTO(form, formRequestDTO);
        return formRepository.save(form);
    }

    public void deleteForm(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found"));
        formRepository.delete(form);
    }

    public List<Form> searchForms(String title, String location) {
        if (title != null && !title.isEmpty() && location != null && !location.isEmpty()) {
            return formRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(title, location);
        } else if (title != null && !title.isEmpty()) {
            return formRepository.findByTitleContainingIgnoreCase(title);
        } else if (location != null && !location.isEmpty()) {
            return formRepository.findByLocationContainingIgnoreCase(location);
        } else {
            return formRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    public List<Form> getPublishedForms() {
        return formRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    public List<Form> getUnpublishedForms() {
        return formRepository.findByPublishedFalse();
    }

    public Form getJobInfoFromEncryptedId(String encryptedId) {
        try {
            // Décrypter l'ID de l'offre
            Long formId = applicationLinkUtil.decryptId(encryptedId);
            System.out.println("ID décrypté: " + formId);

            // Récupérer l'offre
            Optional<Form> formOpt = formRepository.findById(formId);
            if (formOpt.isEmpty()) {
                throw new RuntimeException("Offre d'emploi non trouvée avec l'ID: " + formId);
            }

            Form jobOffer = formOpt.get();
            System.out.println("Offre trouvée: " + jobOffer.getTitle());

            // Retourner l'entité Form directement
            return jobOffer;

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des infos d'offre: " + e.getMessage());
            throw new RuntimeException("Erreur lors du décryptage ou de la récupération: " + e.getMessage());
        }
    }
}