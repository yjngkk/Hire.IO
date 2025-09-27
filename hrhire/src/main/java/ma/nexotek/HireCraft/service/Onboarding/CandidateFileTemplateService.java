package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.dto.CandidateFileTemplateDTO;
import ma.nexotek.HireCraft.model.CandidateFileTemplate;
import ma.nexotek.HireCraft.repository.CandidateFileTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateFileTemplateService {

    @Autowired
    private CandidateFileTemplateRepository templateRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Get all active templates
    public List<CandidateFileTemplateDTO> getAllActiveTemplates() {
        return templateRepository.findAllActiveOrderedByIndex()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all templates (including inactive)
    public List<CandidateFileTemplateDTO> getAllTemplates() {
        return templateRepository.findAllOrderedByIndex()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get template by ID
    public Optional<CandidateFileTemplateDTO> getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Create new template
    public CandidateFileTemplateDTO createTemplate(CandidateFileTemplateDTO templateDTO) {
        // Validate unique name
        if (templateRepository.existsByNameIgnoreCase(templateDTO.getName())) {
            throw new RuntimeException("Un template avec ce nom existe déjà");
        }

        CandidateFileTemplate template = convertToEntity(templateDTO);
        template = templateRepository.save(template);
        return convertToDTO(template);
    }

    // Update template
    public CandidateFileTemplateDTO updateTemplate(Long id, CandidateFileTemplateDTO templateDTO) {
        CandidateFileTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template non trouvé avec l'ID: " + id));

        // Validate unique name (excluding current template)
        if (templateRepository.existsByNameIgnoreCaseAndIdNot(templateDTO.getName(), id)) {
            throw new RuntimeException("Un template avec ce nom existe déjà");
        }

        // Update fields
        existingTemplate.setName(templateDTO.getName());
        existingTemplate.setDescription(templateDTO.getDescription());
        existingTemplate.setOrderIndex(templateDTO.getOrderIndex());
        existingTemplate.setIsRequired(templateDTO.getIsRequired());
        existingTemplate.setIsActive(templateDTO.getIsActive());
        existingTemplate.setDocumentType(templateDTO.getDocumentType());
        existingTemplate.setAcceptedFormats(templateDTO.getAcceptedFormats());

        existingTemplate = templateRepository.save(existingTemplate);
        return convertToDTO(existingTemplate);
    }

    // Delete template (soft delete by setting isActive to false)
    public void deleteTemplate(Long id) {
        CandidateFileTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template non trouvé avec l'ID: " + id));

        template.setIsActive(false);
        templateRepository.save(template);
    }

    // Hard delete template
    public void hardDeleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template non trouvé avec l'ID: " + id);
        }
        templateRepository.deleteById(id);
    }

    // Reactivate template
    public CandidateFileTemplateDTO reactivateTemplate(Long id) {
        CandidateFileTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template non trouvé avec l'ID: " + id));

        template.setIsActive(true);
        template = templateRepository.save(template);
        return convertToDTO(template);
    }

    // Get templates by document type
    public List<CandidateFileTemplateDTO> getTemplatesByDocumentType(String documentType) {
        return templateRepository.findByDocumentTypeAndActiveOrderedByIndex(documentType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get required templates only
    public List<CandidateFileTemplateDTO> getRequiredTemplates() {
        return templateRepository.findRequiredActiveTemplatesOrderedByIndex()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get optional templates only
    public List<CandidateFileTemplateDTO> getOptionalTemplates() {
        return templateRepository.findOptionalActiveTemplatesOrderedByIndex()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Reorder templates
    public List<CandidateFileTemplateDTO> reorderTemplates(List<Long> templateIds) {
        for (int i = 0; i < templateIds.size(); i++) {
            Long templateId = templateIds.get(i);
            CandidateFileTemplate template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template non trouvé avec l'ID: " + templateId));
            template.setOrderIndex(i + 1);
            templateRepository.save(template);
        }

        return getAllActiveTemplates();
    }

    // Convert entity to DTO
    private CandidateFileTemplateDTO convertToDTO(CandidateFileTemplate template) {
        return CandidateFileTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .orderIndex(template.getOrderIndex())
                .isRequired(template.getIsRequired())
                .isActive(template.getIsActive())
                .documentType(template.getDocumentType())
                .acceptedFormats(template.getAcceptedFormats())
                .createdDate(template.getCreatedDate() != null ? template.getCreatedDate().format(formatter) : null)
                .updatedDate(template.getUpdatedDate() != null ? template.getUpdatedDate().format(formatter) : null)
                .build();
    }

    // Convert DTO to entity
    private CandidateFileTemplate convertToEntity(CandidateFileTemplateDTO dto) {
        return CandidateFileTemplate.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .orderIndex(dto.getOrderIndex())
                .isRequired(dto.getIsRequired() != null ? dto.getIsRequired() : true)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .documentType(dto.getDocumentType())
                .acceptedFormats(dto.getAcceptedFormats() != null ? dto.getAcceptedFormats() : ".pdf,.doc,.docx,.jpg,.jpeg,.png")
                .build();
    }
}