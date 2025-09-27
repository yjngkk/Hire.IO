package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.dto.CandidateFileDTO;
import ma.nexotek.HireCraft.dto.OnboardingPlanDTO;
import ma.nexotek.HireCraft.dto.ProcedureDTO;
import ma.nexotek.HireCraft.dto.ProcedureTemplateResponseDTO;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.OnboardingPlan;
import ma.nexotek.HireCraft.model.OnboardingPlan.OnboardingStatus;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.OnboardingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OnboardingPlanService {

    @Autowired
    private OnboardingPlanRepository onboardingPlanRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private ProcedureTemplateService procedureTemplateService;

    @Autowired
    private ProcedureService procedureService;
    @Autowired
    private CandidateFileTemplateService candidateFileTemplateService;

    @Autowired
    private CandidateFileService candidateFileService;

    public List<OnboardingPlanDTO> getAllOnboardingPlans() {
        return onboardingPlanRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<OnboardingPlanDTO> getOnboardingPlanById(Long id) {
        return onboardingPlanRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<OnboardingPlanDTO> getOnboardingPlanByCandidatId(Long candidatId) {
        return onboardingPlanRepository.findByCandidatId(candidatId)
                .map(this::convertToDTO);
    }

    public List<OnboardingPlanDTO> getActiveOnboardingPlans() {
        return onboardingPlanRepository.findActiveOnboardingPlans()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OnboardingPlanDTO> getOnboardingPlansByStatus(String status) {
        OnboardingStatus onboardingStatus = OnboardingStatus.valueOf(status.toUpperCase().replace("-", "_"));
        return onboardingPlanRepository.findByStatus(onboardingStatus)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OnboardingPlanDTO createOnboardingPlan(OnboardingPlanDTO dto) {
        Candidat candidat = candidatRepository.findById(dto.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + dto.getCandidatId()));

        // Check if onboarding plan already exists for this candidat
        if (onboardingPlanRepository.findByCandidatId(dto.getCandidatId()).isPresent()) {
            throw new RuntimeException("Onboarding plan already exists for candidat id: " + dto.getCandidatId());
        }

        OnboardingPlan onboardingPlan = OnboardingPlan.builder()
                .candidat(candidat)
                .startDate(dto.getStartDate())
                .progress(dto.getProgress() != null ? dto.getProgress() : 0)
                .manager(dto.getManager())
                .status(OnboardingStatus.valueOf(dto.getStatus().toUpperCase().replace("-", "_")))
                .expectedEndDate(dto.getExpectedEndDate())
                .notes(dto.getNotes())
                .build();

        OnboardingPlan saved = onboardingPlanRepository.save(onboardingPlan);

        // Create procedures from active templates
        createProceduresFromTemplates(dto.getCandidatId());

        // ADD THIS: Create candidate files from active templates
        createCandidateFilesFromTemplates(dto.getCandidatId());

        return convertToDTO(saved);
    }


    @Transactional
    public OnboardingPlanDTO updateOnboardingPlan(Long id, OnboardingPlanDTO dto) {
        OnboardingPlan existingPlan = onboardingPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Onboarding plan not found with id: " + id));

        existingPlan.setStartDate(dto.getStartDate());
        existingPlan.setProgress(dto.getProgress());
        existingPlan.setManager(dto.getManager());
        existingPlan.setStatus(OnboardingStatus.valueOf(dto.getStatus().toUpperCase().replace("-", "_")));
        existingPlan.setExpectedEndDate(dto.getExpectedEndDate());
        existingPlan.setActualEndDate(dto.getActualEndDate());
        existingPlan.setNotes(dto.getNotes());

        OnboardingPlan updated = onboardingPlanRepository.save(existingPlan);
        return convertToDTO(updated);
    }

    // Add this method to your OnboardingPlanService class

    @Transactional
    public OnboardingPlanDTO updateProgress(Long id, Integer progress) {
        OnboardingPlan plan = onboardingPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Onboarding plan not found with id: " + id));

        plan.setProgress(progress);

        // Optionally update status based on progress
        if (progress == 100) {
            plan.setStatus(OnboardingStatus.COMPLETED);
            plan.setActualEndDate(LocalDate.now());
        } else if (progress > 0 && plan.getStatus() == OnboardingStatus.PENDING) {
            plan.setStatus(OnboardingStatus.IN_PROGRESS);
        }

        OnboardingPlan savedPlan = onboardingPlanRepository.save(plan);
        return convertToDTO(savedPlan);
    }

    @Transactional
    public void deleteOnboardingPlan(Long id) {
        if (!onboardingPlanRepository.existsById(id)) {
            throw new RuntimeException("Onboarding plan not found with id: " + id);
        }
        onboardingPlanRepository.deleteById(id);
    }

    /**
     * Creates procedures from active templates for a specific candidate
     * This method is called automatically when creating an onboarding plan
     */
    private void createProceduresFromTemplates(Long candidatId) {
        try {
            // Get all active procedure templates
            List<ProcedureTemplateResponseDTO> activeTemplates = procedureTemplateService.getAllActiveTemplates();

            // Create procedures from each template
            for (ProcedureTemplateResponseDTO template : activeTemplates) {
                ProcedureDTO procedureDTO = ProcedureDTO.builder()
                        .title(template.getTitle())
                        .responsible(template.getResponsible())
                        .deadline(template.getDeadline())
                        .description(template.getDescription())
                        .candidatId(candidatId)
                        .completed(false)
                        .build();

                try {
                    procedureService.createProcedure(procedureDTO);
                } catch (Exception e) {
                    System.err.println("Failed to create procedure from template " + template.getId() +
                            " for candidat " + candidatId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Log the error but don't fail the entire onboarding plan creation
            System.err.println("Failed to create procedures from templates for candidat " + candidatId + ": " + e.getMessage());
        }
    }


    @Transactional
    public List<ProcedureDTO> generateProceduresFromTemplates(Long candidatId) {
        // Verify that the candidat exists and has an onboarding plan
        if (!onboardingPlanRepository.findByCandidatId(candidatId).isPresent()) {
            throw new RuntimeException("No onboarding plan found for candidat id: " + candidatId);
        }

        List<ProcedureTemplateResponseDTO> activeTemplates = procedureTemplateService.getAllActiveTemplates();
        List<ProcedureDTO> createdProcedures = new java.util.ArrayList<>();

        for (ProcedureTemplateResponseDTO template : activeTemplates) {
            try {
                ProcedureDTO procedureDTO = ProcedureDTO.builder()
                        .title(template.getTitle())
                        .responsible(template.getResponsible())
                        .deadline(template.getDeadline())
                        .description(template.getDescription())
                        .candidatId(candidatId)
                        .completed(false)
                        .build();

                ProcedureDTO createdProcedure = procedureService.createProcedure(procedureDTO);
                createdProcedures.add(createdProcedure);
            } catch (Exception e) {
                // Log the error and continue with other templates
                System.err.println("Failed to create procedure from template " + template.getId() +
                        " for candidat " + candidatId + ": " + e.getMessage());
            }
        }

        return createdProcedures;
    }

    /**
     * Creates candidate files from active templates for a specific candidate
     * This method is called automatically when creating an onboarding plan
     */
    private void createCandidateFilesFromTemplates(Long candidatId) {
        try {
            // Use the existing initializeFilesForCandidat method from CandidateFileService
            candidateFileService.initializeFilesForCandidat(candidatId);
        } catch (Exception e) {
            // Log the error but don't fail the entire onboarding plan creation
            System.err.println("Failed to create candidate files from templates for candidat " + candidatId + ": " + e.getMessage());
        }
    }

    /**
     * Generates candidate files from templates for an existing onboarding plan
     */
    @Transactional
    public List<CandidateFileDTO> generateCandidateFilesFromTemplates(Long candidatId) {
        // Verify that the candidat exists and has an onboarding plan
        if (!onboardingPlanRepository.findByCandidatId(candidatId).isPresent()) {
            throw new RuntimeException("No onboarding plan found for candidat id: " + candidatId);
        }

        try {
            return candidateFileService.initializeFilesForCandidat(candidatId);
        } catch (Exception e) {
            System.err.println("Failed to generate candidate files from templates for candidat " + candidatId + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate candidate files: " + e.getMessage(), e);
        }
    }
    private OnboardingPlanDTO convertToDTO(OnboardingPlan plan) {
        return OnboardingPlanDTO.builder()
                .id(plan.getId())
                .employeeName(plan.getCandidat().getNom())
                .notes(plan.getCandidat().getPoste())
                .employeeEmail(plan.getCandidat().getEmail())
                .startDate(plan.getStartDate())
                .progress(plan.getProgress())
                .manager(plan.getManager())
                .status(plan.getStatus().getValue())
                .expectedEndDate(plan.getExpectedEndDate())
                .actualEndDate(plan.getActualEndDate())
                .notes(plan.getNotes())
                .candidatId(plan.getCandidat().getId())
                .build();
    }
}