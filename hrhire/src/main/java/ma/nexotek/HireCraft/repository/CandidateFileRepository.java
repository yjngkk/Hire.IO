package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.CandidateFile;
import ma.nexotek.HireCraft.model.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateFileRepository extends JpaRepository<CandidateFile, Long> {

    // Find all files for a specific candidat ordered by orderIndex
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId ORDER BY f.orderIndex ASC")
    List<CandidateFile> findByCandidatIdOrderedByIndex(@Param("candidatId") Long candidatId);

    // Find files by candidat and status
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.status = :status ORDER BY f.orderIndex ASC")
    List<CandidateFile> findByCandidatIdAndStatus(@Param("candidatId") Long candidatId, @Param("status") CandidateFile.FileStatus status);

    // Find required files for a candidat
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.isRequired = true ORDER BY f.orderIndex ASC")
    List<CandidateFile> findRequiredFilesByCandidatId(@Param("candidatId") Long candidatId);

    // Find optional files for a candidat
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.isRequired = false ORDER BY f.orderIndex ASC")
    List<CandidateFile> findOptionalFilesByCandidatId(@Param("candidatId") Long candidatId);

    // Count files by status for a candidat
    @Query("SELECT COUNT(f) FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.status = :status")
    long countByCandidatIdAndStatus(@Param("candidatId") Long candidatId, @Param("status") CandidateFile.FileStatus status);

    // Count required files completed for a candidat
    @Query("SELECT COUNT(f) FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.isRequired = true AND f.status = 'COMPLETED'")
    long countRequiredCompletedFilesByCandidatId(@Param("candidatId") Long candidatId);

    // Count total required files for a candidat
    @Query("SELECT COUNT(f) FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.isRequired = true")
    long countRequiredFilesByCandidatId(@Param("candidatId") Long candidatId);

    // Find file by candidat and template
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.template.id = :templateId")
    Optional<CandidateFile> findByCandidatIdAndTemplateId(@Param("candidatId") Long candidatId, @Param("templateId") Long templateId);

    // Find files by document type for a candidat
    @Query("SELECT f FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.documentType = :documentType ORDER BY f.orderIndex ASC")
    List<CandidateFile> findByCandidatIdAndDocumentType(@Param("candidatId") Long candidatId, @Param("documentType") String documentType);

    // Get completion percentage for a candidat
    @Query("SELECT (COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM CandidateFile f2 WHERE f2.candidat.id = :candidatId AND f2.isRequired = true)) " +
            "FROM CandidateFile f WHERE f.candidat.id = :candidatId AND f.isRequired = true AND f.status = 'COMPLETED'")
    Double getCompletionPercentageByCandidatId(@Param("candidatId") Long candidatId);

    // Find all candidats with their file completion status
    @Query("SELECT DISTINCT f.candidat FROM CandidateFile f")
    List<Candidat> findAllCandidatsWithFiles();

    // Find files that need attention (missing or pending for too long)
    @Query("SELECT f FROM CandidateFile f WHERE f.status IN ('MISSING', 'PENDING') AND f.isRequired = true ORDER BY f.candidat.id, f.orderIndex")
    List<CandidateFile> findFilesNeedingAttention();


    @Query("SELECT cf FROM CandidateFile cf WHERE cf.candidat.id = :candidatId AND cf.status IN :statuses")
    List<CandidateFile> findByCandidatIdAndStatusIn(@Param("candidatId") Long candidatId,
                                                    @Param("statuses") List<CandidateFile.FileStatus> statuses);

}
