package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.dto.ProcedureResponseDTO;
import ma.nexotek.HireCraft.dto.ProcedureUpdateDTO;
import ma.nexotek.HireCraft.model.*;
import ma.nexotek.HireCraft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    @Autowired
    private OnboardingPlanRepository onboardingPlanRepository;

    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Transactional
    public OnboardingPlan createOnboardingPlan(Long candidatId, String manager, LocalDate startDate,
                                               LocalDate expectedEndDate, String notes) {
        // 1. Get the candidate
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + candidatId));

        // 2. Check if candidate already has an onboarding plan
        if (onboardingPlanRepository.findByCandidatId(candidatId).isPresent()) {
            throw new RuntimeException("Candidat already has an onboarding plan");
        }

        // 3. Create the onboarding plan
        OnboardingPlan plan = OnboardingPlan.builder()
                .candidat(candidat)
                .manager(manager)
                .startDate(startDate)
                .expectedEndDate(expectedEndDate)
                .notes(notes)
                .status(OnboardingPlan.OnboardingStatus.PENDING)
                .progress(0)
                .build();

        // 4. Save the plan first
        plan = onboardingPlanRepository.save(plan);

        // 5. Get all active procedure templates ordered by orderIndex
        List<ProcedureTemplate> templates = procedureTemplateRepository.findActiveTemplatesOrdered();

        // 6. Create actual procedures from templates for this candidate
        List<Procedure> procedures = new ArrayList<>();
        for (ProcedureTemplate template : templates) {
            Procedure procedure = new Procedure(template, candidat);
            procedures.add(procedure);
        }

        // 7. Save all procedures
        if (!procedures.isEmpty()) {
            procedureRepository.saveAll(procedures);
        }

        return plan;
    }

    public List<ProcedureResponseDTO> getProceduresForCandidat(Long candidatId) {
        List<Procedure> procedures = procedureRepository.findByCandidatIdOrderByOrderIndex(candidatId);
        return procedures.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcedureResponseDTO updateProcedureStatus(Long procedureId, ProcedureUpdateDTO updateDTO) {
        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Procedure not found with id: " + procedureId));

        procedure.setCompleted(updateDTO.getCompleted());
        procedure = procedureRepository.save(procedure);

        // Update onboarding plan progress
        updateOnboardingProgress(procedure.getCandidat().getId());

        return convertToResponseDTO(procedure);
    }

    public OnboardingPlan getOnboardingPlanByCandidat(Long candidatId) {
        return onboardingPlanRepository.findByCandidatId(candidatId)
                .orElseThrow(() -> new RuntimeException("Onboarding plan not found for candidat id: " + candidatId));
    }

    public List<OnboardingPlan> getAllOnboardingPlans() {
        return onboardingPlanRepository.findAll();
    }

    @Transactional
    public void deleteOnboardingPlan(Long planId) {
        OnboardingPlan plan = onboardingPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Onboarding plan not found with id: " + planId));

        // Delete associated procedures first
        List<Procedure> procedures = procedureRepository.findByCandidatId(plan.getCandidat().getId());
        procedureRepository.deleteAll(procedures);

        // Delete onboarding plan
        onboardingPlanRepository.delete(plan);
    }

    private void updateOnboardingProgress(Long candidatId) {
        long totalProcedures = procedureRepository.countByCandidatId(candidatId);
        long completedProcedures = procedureRepository.countCompletedByCandidatId(candidatId);

        if (totalProcedures > 0) {
            int progress = (int) ((completedProcedures * 100) / totalProcedures);

            // Update the onboarding plan progress
            OnboardingPlan plan = onboardingPlanRepository.findByCandidatId(candidatId)
                    .orElseThrow(() -> new RuntimeException("Onboarding plan not found"));

            plan.setProgress(progress);

            // Update status based on progress
            if (progress == 100) {
                plan.setStatus(OnboardingPlan.OnboardingStatus.COMPLETED);
                if (plan.getActualEndDate() == null) {
                    plan.setActualEndDate(LocalDate.now());
                }
            } else if (progress > 0 && plan.getStatus() == OnboardingPlan.OnboardingStatus.PENDING) {
                plan.setStatus(OnboardingPlan.OnboardingStatus.IN_PROGRESS);
            }

            onboardingPlanRepository.save(plan);
        }
    }

    // Helper method to convert Procedure entity to DTO
    private ProcedureResponseDTO convertToResponseDTO(Procedure procedure) {
        return ProcedureResponseDTO.builder()
                .id(procedure.getId())
                .title(procedure.getTitle())
                .responsible(procedure.getResponsible())
                .deadline(procedure.getDeadlineFormat())
                .description(procedure.getDescription())
                .orderIndex(procedure.getOrderIndex())
                .completed(procedure.getCompleted())
                .createdDate(procedure.getCreatedDate())
                .updatedDate(procedure.getUpdatedDate())
                .templateId(procedure.getTemplate() != null ? procedure.getTemplate().getId() : null)
                .templateTitle(procedure.getTemplate() != null ? procedure.getTemplate().getTitle() : null)
                .candidatId(procedure.getCandidat().getId())
                .candidatName(procedure.getCandidat().getNom())
                .build();
    }
}