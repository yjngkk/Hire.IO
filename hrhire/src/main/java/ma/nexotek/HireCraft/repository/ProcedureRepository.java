package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Procedure;
import ma.nexotek.HireCraft.model.ProcedureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    List<Procedure> findByCandidatId(Long candidatId);

    // Find procedures by candidat ID ordered by orderIndex
    @Query("SELECT p FROM Procedure p WHERE p.candidat.id = :candidatId ORDER BY p.orderIndex ASC")
    List<Procedure> findByCandidatIdOrderByOrderIndex(@Param("candidatId") Long candidatId);


    // Count completed procedures for a candidat
    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.candidat.id = :candidatId AND p.completed = true")
    long countCompletedByCandidatId(@Param("candidatId") Long candidatId);

    // Count total procedures for a candidat
    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.candidat.id = :candidatId")
    long countByCandidatId(@Param("candidatId") Long candidatId);

    List<Procedure> findByStatus(ProcedureStatus status);
    List<Procedure> findByStatusAndCompletedFalse(ProcedureStatus status);

    // Find incomplete procedures
    List<Procedure> findByCompletedFalse();

    // Find by due date range
    List<Procedure> findByCompletedFalseAndDueDateBetweenOrderByDueDate(
            LocalDateTime startDate, LocalDateTime endDate);


    long countByCandidatIdAndCompletedFalse(Long candidatId);


    @Query("SELECT p FROM Procedure p WHERE p.completed = false " +
            "AND p.dueDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.dueDate, p.candidat.nom")
    List<Procedure> findUpcomingProcedures(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}