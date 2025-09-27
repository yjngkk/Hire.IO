package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByStatus(String status);
} 