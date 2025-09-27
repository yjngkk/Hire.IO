package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.ZoomMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ZoomMeetingRepository extends JpaRepository<ZoomMeeting, Long> {
    List<ZoomMeeting> findByCandidatId(Long candidatId);
}
