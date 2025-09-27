package ma.nexotek.HireCraft.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import ma.nexotek.HireCraft.dto.FormRequestDTO;
import ma.nexotek.HireCraft.dto.FormResponseDTO;
import ma.nexotek.HireCraft.mapper.FormMapper;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.service.FormService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;
    private final FormMapper formMapper;

    @PostMapping
    public ResponseEntity<FormResponseDTO> createForm(@Valid @RequestBody FormRequestDTO formRequestDTO) {
        try {
            Form form = formMapper.toEntity(formRequestDTO);
            Form savedForm = formService.createForm(form);
            FormResponseDTO responseDTO = formMapper.toResponseDTO(savedForm);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FormResponseDTO>> getAllForms() {
        List<Form> forms = formService.getAllForms();
        List<FormResponseDTO> responseDTOs = formMapper.toResponseDTOList(forms);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormResponseDTO> getFormById(@PathVariable Long id) {
        try {
            Form form = formService.getFormById(id);
            FormResponseDTO responseDTO = formMapper.toResponseDTO(form);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormResponseDTO> updateForm(@PathVariable Long id,
                                                      @Valid @RequestBody FormRequestDTO formRequestDTO) {
        try {
            Form updatedForm = formService.updateForm(id, formRequestDTO);
            FormResponseDTO responseDTO = formMapper.toResponseDTO(updatedForm);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        try {
            formService.deleteForm(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<FormResponseDTO>> searchForms(@RequestParam(required = false) String title,
                                                             @RequestParam(required = false) String location) {
        List<Form> forms = formService.searchForms(title, location);
        List<FormResponseDTO> responseDTOs = formMapper.toResponseDTOList(forms);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/published")
    public ResponseEntity<List<FormResponseDTO>> getPublishedForms() {
        List<Form> forms = formService.getPublishedForms();
        List<FormResponseDTO> responseDTOs = formMapper.toResponseDTOList(forms);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/unpublished")
    public ResponseEntity<List<FormResponseDTO>> getUnpublishedForms() {
        List<Form> forms = formService.getUnpublishedForms();
        List<FormResponseDTO> responseDTOs = formMapper.toResponseDTOList(forms);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/info/{encryptedId}")
    public ResponseEntity<?> getJobInfo(@PathVariable String encryptedId) {
        try {
            Form jobInfo = formService.getJobInfoFromEncryptedId(encryptedId);
            return ResponseEntity.ok(jobInfo);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Lien invalide: " + e.getMessage());
        }
    }
}