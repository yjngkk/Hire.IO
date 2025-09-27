package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByNameContainingIgnoreCase(String name);
    List<Document> findByTypeContainingIgnoreCase(String type);
    Optional<Document> findByFilePath(String filePath);
}

