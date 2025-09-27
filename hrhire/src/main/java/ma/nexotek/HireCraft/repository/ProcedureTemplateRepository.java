package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.ProcedureTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureTemplateRepository extends JpaRepository<ProcedureTemplate, Long> {

    // Find all active templates ordered by orderIndex
    @Query("SELECT pt FROM ProcedureTemplate pt WHERE pt.isActive = true ORDER BY pt.orderIndex ASC")
    List<ProcedureTemplate> findActiveTemplatesOrdered();

    // Find templates by responsible party
    List<ProcedureTemplate> findByResponsibleAndIsActiveTrue(String responsible);

    // Find templates by active status
    List<ProcedureTemplate> findByIsActiveOrderByOrderIndex(Boolean isActive);

    // Count active templates
    @Query("SELECT COUNT(pt) FROM ProcedureTemplate pt WHERE pt.isActive = true")
    long countActiveTemplates();
}