package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.OnboardingPlan;
import ma.nexotek.HireCraft.model.OnboardingPlan.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingPlanRepository extends JpaRepository<OnboardingPlan, Long> {

    // Find by candidat ID
    Optional<OnboardingPlan> findByCandidatId(Long candidatId);

    // Find all plans by status
    List<OnboardingPlan> findByStatus(OnboardingStatus status);

    // Find all active onboarding plans (in progress or pending)
    @Query("SELECT op FROM OnboardingPlan op WHERE op.status IN ('PENDING', 'IN_PROGRESS')")
    List<OnboardingPlan> findActiveOnboardingPlans();

    // Find plans by manager
    List<OnboardingPlan> findByManagerIgnoreCase(String manager);

    // Find plans with progress less than specified value
    List<OnboardingPlan> findByProgressLessThan(Integer progress);

    // Custom query to get onboarding plans with candidat details
    @Query("SELECT op FROM OnboardingPlan op JOIN FETCH op.candidat WHERE op.status = :status")
    List<OnboardingPlan> findByStatusWithCandidat(@Param("status") OnboardingStatus status);

    // Find onboarding plans by manager
    List<OnboardingPlan> findByManager(String manager);

    // Find onboarding plans with start date between dates
    @Query("SELECT op FROM OnboardingPlan op WHERE op.startDate BETWEEN :startDate AND :endDate")
    List<OnboardingPlan> findByStartDateBetween(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Find overdue onboarding plans (expected end date passed but not completed)
    @Query("SELECT op FROM OnboardingPlan op WHERE op.expectedEndDate < :currentDate AND op.status != :completedStatus")
    List<OnboardingPlan> findOverduePlans(@Param("currentDate") LocalDate currentDate,
                                          @Param("completedStatus") OnboardingStatus completedStatus);

    // Find onboarding plans by progress range
    @Query("SELECT op FROM OnboardingPlan op WHERE op.progress BETWEEN :minProgress AND :maxProgress")
    List<OnboardingPlan> findByProgressBetween(@Param("minProgress") Integer minProgress,
                                               @Param("maxProgress") Integer maxProgress);

    // Count onboarding plans by status
    long countByStatus(OnboardingStatus status);

    // Find recent onboarding plans (created within last N days)
    @Query("SELECT op FROM OnboardingPlan op WHERE op.createdAt >= :sinceDate ORDER BY op.createdAt DESC")
    List<OnboardingPlan> findRecentPlans(@Param("sinceDate") java.time.LocalDateTime sinceDate);
}
