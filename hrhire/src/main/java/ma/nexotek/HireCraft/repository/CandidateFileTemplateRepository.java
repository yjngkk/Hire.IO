package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.CandidateFileTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateFileTemplateRepository extends JpaRepository<CandidateFileTemplate, Long> {

    // Find all active templates ordered by orderIndex
    @Query("SELECT t FROM CandidateFileTemplate t WHERE t.isActive = true ORDER BY t.orderIndex ASC")
    List<CandidateFileTemplate> findAllActiveOrderedByIndex();

    // Find all templates (including inactive) ordered by orderIndex
    @Query("SELECT t FROM CandidateFileTemplate t ORDER BY t.orderIndex ASC")
    List<CandidateFileTemplate> findAllOrderedByIndex();

    // Count active templates
    @Query("SELECT COUNT(t) FROM CandidateFileTemplate t WHERE t.isActive = true")
    long countActiveTemplates();

    // Find templates by document type
    @Query("SELECT t FROM CandidateFileTemplate t WHERE t.documentType = :documentType AND t.isActive = true ORDER BY t.orderIndex ASC")
    List<CandidateFileTemplate> findByDocumentTypeAndActiveOrderedByIndex(String documentType);

    // Find required templates only
    @Query("SELECT t FROM CandidateFileTemplate t WHERE t.isRequired = true AND t.isActive = true ORDER BY t.orderIndex ASC")
    List<CandidateFileTemplate> findRequiredActiveTemplatesOrderedByIndex();

    // Find optional templates only
    @Query("SELECT t FROM CandidateFileTemplate t WHERE t.isRequired = false AND t.isActive = true ORDER BY t.orderIndex ASC")
    List<CandidateFileTemplate> findOptionalActiveTemplatesOrderedByIndex();

    // Check if template name exists (for validation)
    @Query("SELECT COUNT(t) > 0 FROM CandidateFileTemplate t WHERE LOWER(t.name) = LOWER(:name) AND t.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long excludeId);

    // Overloaded method for new templates
    @Query("SELECT COUNT(t) > 0 FROM CandidateFileTemplate t WHERE LOWER(t.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(String name);


}
