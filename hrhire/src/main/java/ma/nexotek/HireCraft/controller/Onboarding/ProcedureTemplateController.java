package ma.nexotek.HireCraft.controller.Onboarding;


import ma.nexotek.HireCraft.dto.ProcedureTemplateRequestDTO;
import ma.nexotek.HireCraft.dto.ProcedureTemplateResponseDTO;
import ma.nexotek.HireCraft.service.Onboarding.ProcedureTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/procedure-templates")
public class ProcedureTemplateController {

    @Autowired
    private ProcedureTemplateService procedureTemplateService;

    @GetMapping
    public ResponseEntity<List<ProcedureTemplateResponseDTO>> getAllTemplates(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<ProcedureTemplateResponseDTO> templates;

        if (activeOnly) {
            templates = procedureTemplateService.getAllActiveTemplates();
        } else {
            templates = procedureTemplateService.getAllTemplates();
        }

        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureTemplateResponseDTO> getTemplateById(@PathVariable Long id) {
        try {
            ProcedureTemplateResponseDTO template = procedureTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-responsible/{responsible}")
    public ResponseEntity<List<ProcedureTemplateResponseDTO>> getTemplatesByResponsible(
            @PathVariable String responsible) {
        List<ProcedureTemplateResponseDTO> templates =
                procedureTemplateService.getTemplatesByResponsible(responsible);
        return ResponseEntity.ok(templates);
    }

    @PostMapping
    public ResponseEntity<ProcedureTemplateResponseDTO> createTemplate(
            @Valid @RequestBody ProcedureTemplateRequestDTO requestDTO) {
        try {
            ProcedureTemplateResponseDTO createdTemplate =
                    procedureTemplateService.createTemplate(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureTemplateResponseDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody ProcedureTemplateRequestDTO requestDTO) {
        try {
            ProcedureTemplateResponseDTO updatedTemplate =
                    procedureTemplateService.updateTemplate(id, requestDTO);
            return ResponseEntity.ok(updatedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            procedureTemplateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteTemplate(@PathVariable Long id) {
        try {
            procedureTemplateService.hardDeleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ProcedureTemplateResponseDTO> toggleTemplateActive(@PathVariable Long id) {
        try {
            ProcedureTemplateResponseDTO template = procedureTemplateService.getTemplateById(id);

            ProcedureTemplateRequestDTO updateRequest = ProcedureTemplateRequestDTO.builder()
                    .title(template.getTitle())
                    .responsible(template.getResponsible())
                    .deadline(template.getDeadline())
                    .description(template.getDescription())
                    .orderIndex(template.getOrderIndex())
                    .isActive(!template.getIsActive()) // Toggle the active status
                    .build();

            ProcedureTemplateResponseDTO updatedTemplate =
                    procedureTemplateService.updateTemplate(id, updateRequest);
            return ResponseEntity.ok(updatedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}