package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    List<Candidat> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByCreatedAtAfter(LocalDateTime dateTime);


    List<Candidat> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);
    // Find candidats without onboarding plans
    @Query("SELECT c FROM Candidat c WHERE c.onboardingPlan IS NULL")
    List<Candidat> findCandidatsWithoutOnboardingPlans();

    // Find candidats with completed onboarding
    @Query("SELECT c FROM Candidat c WHERE c.onboardingPlan.status = 'COMPLETED'")
    List<Candidat> findCandidatsWithCompletedOnboarding();

    // Find candidats with ongoing onboarding
    @Query("SELECT c FROM Candidat c WHERE c.onboardingPlan.status IN ('PENDING', 'IN_PROGRESS')")
    List<Candidat> findCandidatsWithOngoingOnboarding();

    // Find only good talents
    List<Candidat> findByBonTalentTrue();

    @Query(value = "SELECT c.* FROM candidat c LEFT JOIN formulaire f ON c.offre_id = f.id " +
            "ORDER BY c.createdat DESC LIMIT :limit", nativeQuery = true)
    List<Candidat> findTopCandidatsOrderByCreatedAtDesc(@Param("limit") int limit);

}