package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {


    @Query("SELECT e FROM Exercise e JOIN e.tests t WHERE t.id = :testId")
    List<Exercise> findByTestId(@Param("testId") Long testId);
    /**
     * Récupérer un exercice avec toutes ses questions et réponses
     * Utilise JOIN FETCH pour éviter le problème N+1
     */
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "LEFT JOIN FETCH e.questions q " +
            "LEFT JOIN FETCH q.answers " +
            "WHERE e.id = :id")
    Optional<Exercise> findByIdWithQuestionsAndAnswers(@Param("id") Long id);

    /**
     * Récupérer plusieurs exercices avec leurs questions et réponses
     */
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "LEFT JOIN FETCH e.questions q " +
            "LEFT JOIN FETCH q.answers " +
            "WHERE e.id IN :ids")
    List<Exercise> findByIdsWithQuestionsAndAnswers(@Param("ids") List<Long> ids);

    /**
     * Trouver tous les exercices qui ne sont pas dans la liste d'IDs fournie
     */
    List<Exercise> findByIdNotIn(List<Long> ids);

    /**
     * Trouver tous les exercices qui ne sont pas déjà dans un test spécifique
     */
    @Query("SELECT e FROM Exercise e WHERE e.id NOT IN " +
            "(SELECT ex.id FROM Test t JOIN t.exercises ex WHERE t.id = :testId)")
    List<Exercise> findExercisesNotInTest(@Param("testId") Long testId);

    /**
     * Vérifier si un exercice est utilisé dans au moins un test
     */
    @Query("SELECT COUNT(t) > 0 FROM Test t JOIN t.exercises e WHERE e.id = :exerciseId")
    boolean isExerciseUsedInAnyTest(@Param("exerciseId") Long exerciseId);

    /**
     * Compter le nombre de tests utilisant un exercice
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Test t JOIN t.exercises e WHERE e.id = :exerciseId")
    long countTestsUsingExercise(@Param("exerciseId") Long exerciseId);

    /**
     * Obtenir la liste des tests utilisant un exercice
     */
    @Query("SELECT t.name FROM Test t JOIN t.exercises e WHERE e.id = :exerciseId")
    List<String> findTestNamesUsingExercise(@Param("exerciseId") Long exerciseId);
}