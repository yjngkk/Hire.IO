package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Test t LEFT JOIN FETCH t.exercises WHERE t.id = :id")
    Optional<Test> findByIdWithExercises(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Test t LEFT JOIN FETCH t.exercises")
    List<Test> findAllWithExercises();

    List<Test> findByExercisesContaining(Exercise exercise);
    List<Test> findByCategorieAndStatus(String categorie, String status);
    List<Test> findByCategorie(String categorie);

}