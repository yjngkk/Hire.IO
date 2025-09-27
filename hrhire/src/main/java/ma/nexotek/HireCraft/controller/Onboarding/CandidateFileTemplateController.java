package ma.nexotek.HireCraft.controller.Onboarding;


import ma.nexotek.HireCraft.dto.CandidateFileTemplateDTO;
import ma.nexotek.HireCraft.service.Onboarding.CandidateFileTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate-file-templates")
@CrossOrigin(origins = "*")
public class CandidateFileTemplateController {

    @Autowired
    private CandidateFileTemplateService templateService;

    // Get all active templates
    @GetMapping
    public ResponseEntity<List<CandidateFileTemplateDTO>> getAllActiveTemplates() {
        List<CandidateFileTemplateDTO> templates = templateService.getAllActiveTemplates();
        return ResponseEntity.ok(templates);
    }

    // Get all templates (including inactive)
    @GetMapping("/all")
    public ResponseEntity<List<CandidateFileTemplateDTO>> getAllTemplates() {
        List<CandidateFileTemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    // Get template by ID
    @GetMapping("/{id}")
    public ResponseEntity<CandidateFileTemplateDTO> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(template -> ResponseEntity.ok(template))
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new template
    @PostMapping
    public ResponseEntity<?> createTemplate(@Valid @RequestBody CandidateFileTemplateDTO templateDTO) {
        try {
            CandidateFileTemplateDTO createdTemplate = templateService.createTemplate(templateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update template
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @Valid @RequestBody CandidateFileTemplateDTO templateDTO) {
        try {
            CandidateFileTemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDTO);
            return ResponseEntity.ok(updatedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete template (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.ok().body(Map.of("message", "Template désactivé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Hard delete template
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<?> hardDeleteTemplate(@PathVariable Long id) {
        try {
            templateService.hardDeleteTemplate(id);
            return ResponseEntity.ok().body(Map.of("message", "Template supprimé définitivement"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Reactivate template
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateTemplate(@PathVariable Long id) {
        try {
            CandidateFileTemplateDTO reactivatedTemplate = templateService.reactivateTemplate(id);
            return ResponseEntity.ok(reactivatedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get templates by document type
    @GetMapping("/by-type/{documentType}")
    public ResponseEntity<List<CandidateFileTemplateDTO>> getTemplatesByDocumentType(@PathVariable String documentType) {
        List<CandidateFileTemplateDTO> templates = templateService.getTemplatesByDocumentType(documentType);
        return ResponseEntity.ok(templates);
    }

    // Get required templates only
    @GetMapping("/required")
    public ResponseEntity<List<CandidateFileTemplateDTO>> getRequiredTemplates() {
        List<CandidateFileTemplateDTO> templates = templateService.getRequiredTemplates();
        return ResponseEntity.ok(templates);
    }

    // Get optional templates only
    @GetMapping("/optional")
    public ResponseEntity<List<CandidateFileTemplateDTO>> getOptionalTemplates() {
        List<CandidateFileTemplateDTO> templates = templateService.getOptionalTemplates();
        return ResponseEntity.ok(templates);
    }

    // Reorder templates
    @PutMapping("/reorder")
    public ResponseEntity<List<CandidateFileTemplateDTO>> reorderTemplates(@RequestBody List<Long> templateIds) {
        List<CandidateFileTemplateDTO> reorderedTemplates = templateService.reorderTemplates(templateIds);
        return ResponseEntity.ok(reorderedTemplates);
    }
}
