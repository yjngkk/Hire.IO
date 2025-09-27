package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.CandidatAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatAnswerRepository extends JpaRepository<CandidatAnswer, Long> {

    List<CandidatAnswer> findByTestAssignmentId(Long testAssignmentId);

    List<CandidatAnswer> findByTestAssignmentIdAndIsCorrect(Long testAssignmentId, Boolean isCorrect);

    @Query("SELECT COUNT(ca) FROM CandidatAnswer ca WHERE ca.testAssignment.id = :assignmentId AND ca.isCorrect = true")
    Long countCorrectAnswersByAssignment(@Param("assignmentId") Long assignmentId);

    @Query("SELECT SUM(ca.pointsEarned) FROM CandidatAnswer ca WHERE ca.testAssignment.id = :assignmentId")
    Integer calculateTotalScoreByAssignment(@Param("assignmentId") Long assignmentId);
}