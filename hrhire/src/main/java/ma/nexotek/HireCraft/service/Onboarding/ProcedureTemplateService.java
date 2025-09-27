package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.dto.ProcedureTemplateRequestDTO;
import ma.nexotek.HireCraft.dto.ProcedureTemplateResponseDTO;
import ma.nexotek.HireCraft.model.ProcedureTemplate;
import ma.nexotek.HireCraft.repository.ProcedureTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcedureTemplateService {

    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;

    public List<ProcedureTemplateResponseDTO> getAllActiveTemplates() {
        List<ProcedureTemplate> templates = procedureTemplateRepository.findActiveTemplatesOrdered();
        return templates.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProcedureTemplateResponseDTO> getAllTemplates() {
        List<ProcedureTemplate> templates = procedureTemplateRepository.findAll();
        return templates.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProcedureTemplateResponseDTO getTemplateById(Long id) {
        ProcedureTemplate template = procedureTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcedureTemplate not found with id: " + id));
        return convertToResponseDTO(template);
    }

    @Transactional
    public ProcedureTemplateResponseDTO createTemplate(ProcedureTemplateRequestDTO requestDTO) {
        // If orderIndex is not provided, set it to the next available number
        if (requestDTO.getOrderIndex() == null) {
            long count = procedureTemplateRepository.countActiveTemplates();
            requestDTO.setOrderIndex((int) (count + 1));
        }

        ProcedureTemplate template = ProcedureTemplate.builder()
                .title(requestDTO.getTitle())
                .responsible(requestDTO.getResponsible())
                .deadline(requestDTO.getDeadline())
                .description(requestDTO.getDescription())
                .orderIndex(requestDTO.getOrderIndex())
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .build();

        ProcedureTemplate savedTemplate = procedureTemplateRepository.save(template);
        return convertToResponseDTO(savedTemplate);
    }

    @Transactional
    public ProcedureTemplateResponseDTO updateTemplate(Long id, ProcedureTemplateRequestDTO requestDTO) {
        ProcedureTemplate template = procedureTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcedureTemplate not found with id: " + id));

        template.setTitle(requestDTO.getTitle());
        template.setResponsible(requestDTO.getResponsible());
        template.setDeadline(requestDTO.getDeadline());
        template.setDescription(requestDTO.getDescription());

        if (requestDTO.getOrderIndex() != null) {
            template.setOrderIndex(requestDTO.getOrderIndex());
        }

        if (requestDTO.getIsActive() != null) {
            template.setIsActive(requestDTO.getIsActive());
        }

        ProcedureTemplate updatedTemplate = procedureTemplateRepository.save(template);
        return convertToResponseDTO(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        ProcedureTemplate template = procedureTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcedureTemplate not found with id: " + id));

        // Soft delete - just mark as inactive
        template.setIsActive(false);
        procedureTemplateRepository.save(template);
    }

    @Transactional
    public void hardDeleteTemplate(Long id) {
        if (!procedureTemplateRepository.existsById(id)) {
            throw new RuntimeException("ProcedureTemplate not found with id: " + id);
        }
        procedureTemplateRepository.deleteById(id);
    }

    public List<ProcedureTemplateResponseDTO> getTemplatesByResponsible(String responsible) {
        List<ProcedureTemplate> templates = procedureTemplateRepository.findByResponsibleAndIsActiveTrue(responsible);
        return templates.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to DTO
    private ProcedureTemplateResponseDTO convertToResponseDTO(ProcedureTemplate template) {
        return ProcedureTemplateResponseDTO.builder()
                .id(template.getId())
                .title(template.getTitle())
                .responsible(template.getResponsible())
                .deadline(template.getDeadline())
                .description(template.getDescription())
                .orderIndex(template.getOrderIndex())
                .isActive(template.getIsActive())
                .createdDate(template.getCreatedDate())
                .updatedDate(template.getUpdatedDate())
                .build();
    }
}
