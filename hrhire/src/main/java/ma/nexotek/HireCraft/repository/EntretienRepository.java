package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    List<Entretien> findByStatut(String statut);
    
    @Query("SELECT DISTINCT e FROM Entretien e LEFT JOIN FETCH e.candidat c")
    List<Entretien> findAllWithCandidatAndCv();
}