package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.CVScoringResult;
import ma.nexotek.HireCraft.model.DecisionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CVScoringResultRepository extends JpaRepository<CVScoringResult, Long> {
    
    // Trouver le dernier scoring pour un candidat et un job
    Optional<CVScoringResult> findByCandidatIdAndFormId(Long candidatId, Long formId);
    
    // Tous les candidats scorés pour un job, triés par score
    List<CVScoringResult> findByFormIdOrderByFinalScoreDesc(Long formId);
    
    // Candidats acceptés/interview pour un job
    List<CVScoringResult> findByFormIdAndAiDecisionInOrderByFinalScoreDesc(
        Long formId, List<DecisionType> decisions);
    
    // Top candidats pour un job
    List<CVScoringResult> findTop10ByFormIdOrderByFinalScoreDesc(Long formId);
    
    // Statistiques par job
    @Query("SELECT AVG(r.finalScore) FROM CVScoringResult r WHERE r.formId = :formId")
    Double findAverageScoreByForm(@Param("formId") Long formId);
    
    @Query("SELECT COUNT(r) FROM CVScoringResult r WHERE r.formId = :formId AND r.aiDecision = :decision")
    Long countByFormAndDecision(@Param("formId") Long formId, @Param("decision") DecisionType decision);
    
    // Historique des scorings pour un candidat
    List<CVScoringResult> findByCandidatIdOrderByScoringDateDesc(Long candidatId);
    
    // Scorings récents
    List<CVScoringResult> findByScoringDateAfterOrderByScoringDateDesc(LocalDateTime date);
    
    // Vérifier si un candidat a déjà été scoré pour un job
    boolean existsByCandidatIdAndFormId(Long candidatId, Long formId);
    
    // Meilleurs candidats tous jobs confondus
    @Query("SELECT r FROM CVScoringResult r WHERE r.aiDecision IN ('ACCEPT', 'INTERVIEW') ORDER BY r.finalScore DESC")
    List<CVScoringResult> findTopCandidatesAllJobs();
}