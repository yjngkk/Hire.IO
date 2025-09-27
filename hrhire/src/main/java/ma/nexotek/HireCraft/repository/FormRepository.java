package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {

     List<Form> findAllByOrderByCreatedAtDesc();
    List<Form> findByTitleContainingIgnoreCase(String title);
    List<Form> findByLocationContainingIgnoreCase(String location);
    List<Form> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(String title, String location);
    /**
     * Recherche par titre (version simple pour compatibilité avec le service)
     */
    List<Form> findByTitleContaining(String title);
    
    /**
     * Récupère tous les formulaires qui ont du contenu généré
     */
    @Query("SELECT f FROM Form f WHERE f.generatedContent IS NOT NULL")
    List<Form> findAllWithGeneratedContent();
    
    /**
     * Recherche par type de contrat
     */
    List<Form> findByContractTypeContainingIgnoreCase(String contractType);
    
    /**
     * Recherche par niveau d'expérience
     */
    List<Form> findByLevelContainingIgnoreCase(String level);
    
    /**
     * Recherche par plateforme cible
     */
    List<Form> findByTargetPlatformIgnoreCase(String targetPlatform);
    
    /**
     * Recherche par modèle utilisé
     */
    List<Form> findByModelUsedIgnoreCase(String modelUsed);
    
    /**
     * Recherche avancée par plusieurs critères
     */
    @Query("SELECT f FROM Form f WHERE " +
           "(:title IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(f.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:contractType IS NULL OR LOWER(f.contractType) LIKE LOWER(CONCAT('%', :contractType, '%'))) AND " +
           "(:level IS NULL OR LOWER(f.level) LIKE LOWER(CONCAT('%', :level, '%')))")
    List<Form> findByMultipleCriteria(
        @Param("title") String title,
        @Param("location") String location,
        @Param("contractType") String contractType,
        @Param("level") String level
    );

    /**
     * Récupère toutes les offres publiées
     */
    List<Form> findByPublishedTrue();

    /**
     * Récupère toutes les offres non publiées
     */
    List<Form> findByPublishedFalse();

    /**
     * Récupère toutes les offres publiées ordonnées par date de publication
     */
    List<Form> findByPublishedTrueOrderByPublishedAtDesc();




}
