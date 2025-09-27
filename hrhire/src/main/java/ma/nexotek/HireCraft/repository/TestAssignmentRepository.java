package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestAssignmentRepository extends JpaRepository<TestAssignment, Long> {

    Optional<TestAssignment> findByAccessToken(String accessToken);
    // Dans votre repository
    @Query("SELECT ta FROM TestAssignment ta " +
            "LEFT JOIN FETCH ta.candidat c " +
            "LEFT JOIN FETCH c.cv " +
            "LEFT JOIN FETCH ta.test t " +
            "WHERE ta.accessToken = :accessToken " +    // ✅ WHERE avant ORDER BY
            "ORDER BY ta.sentAt DESC")
    Optional<TestAssignment> findByAccessTokenWithTest(@Param("accessToken") String accessToken);

    List<TestAssignment> findByCandidatId(Long candidatId);

    List<TestAssignment> findByStatusIn(List<String> statuses);

    Optional<TestAssignment> findByCandidatAndTestAndStatusIn(
            Candidat candidat, Test test, List<String> statuses);

    @Query("SELECT ta FROM TestAssignment ta WHERE ta.expiresAt < :currentTime AND ta.status IN ('SENT', 'STARTED')")
    List<TestAssignment> findExpiredAssignments(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(ta) FROM TestAssignment ta WHERE ta.candidat.id = :candidatId AND ta.status = 'COMPLETED'")
    Long countCompletedTestsByCandidat(@Param("candidatId") Long candidatId);

    @Query("SELECT ta FROM TestAssignment ta " +
            "LEFT JOIN FETCH ta.candidat c " +         // Charge le candidat
            "LEFT JOIN FETCH c.cv " +                  // Charge le CV du candidat
            "LEFT JOIN FETCH ta.test t " +             // Charge le test
            "ORDER BY ta.sentAt DESC")                 // Trier par date d'envoi
    List<TestAssignment> findAllWithDetails();
    List<TestAssignment> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
