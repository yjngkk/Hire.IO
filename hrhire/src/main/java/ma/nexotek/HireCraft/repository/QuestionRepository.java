package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExerciseId(Long exerciseId);

    List<Question> findByExerciseIdOrderById(Long exerciseId);

    @Query("SELECT q FROM Question q JOIN q.exercise e JOIN e.tests t WHERE t.id = :testId")
    List<Question> findByTestId(@Param("testId") Long testId);
}