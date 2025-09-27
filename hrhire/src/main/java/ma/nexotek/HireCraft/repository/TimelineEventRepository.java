package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.TimelineEvent;
import ma.nexotek.HireCraft.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent> findByCandidatIdOrderByEventDateDesc(Long candidatId);

    List<TimelineEvent> findByCandidatIdAndStatusOrderByEventDateDesc(Long candidatId, EventStatus status);

    @Query("SELECT t FROM TimelineEvent t WHERE t.candidat.id = :candidatId AND t.eventType = :eventType ORDER BY t.eventDate DESC")
    List<TimelineEvent> findByCandidatIdAndEventType(@Param("candidatId") Long candidatId, @Param("eventType") String eventType);
}

