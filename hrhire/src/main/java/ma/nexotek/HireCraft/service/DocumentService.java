package ma.nexotek.HireCraft.service;

import jakarta.transaction.Transactional;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final Path storageLocation;
    private final boolean useFilesystem;

    public DocumentService(DocumentRepository documentRepository,
                           @Value("${document.storage.directory:/tmp/documents}") String storageDir) {
        this.documentRepository = documentRepository;

        // Vérifie si on doit utiliser le filesystem
        this.useFilesystem = storageDir != null && !storageDir.trim().isEmpty();

        if (useFilesystem) {
            this.storageLocation = Paths.get(storageDir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(this.storageLocation);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create document storage directory", ex);
            }
        } else {
            this.storageLocation = null;
        }
    }

    public Document uploadDocument(MultipartFile file) {
        try {
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalName.contains("..")) {
                throw new RuntimeException("Invalid filename: " + originalName);
            }

            byte[] fileContent = file.getBytes();
            String filePath = null;

            // Stockage conditionnel sur filesystem
            if (useFilesystem) {
                String extension = "";
                if (originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String storedName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;

                Path targetPath = this.storageLocation.resolve(storedName);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                filePath = targetPath.toString();
            }

            // Stockage en BDD (toujours)
            Document document = new Document(
                    originalName,
                    filePath, // null si pas de filesystem
                    file.getContentType(),
                    file.getSize(),
                    fileContent // toujours stocké en BDD
            );

            return documentRepository.save(document);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store document: " + file.getOriginalFilename(), ex);
        }
    }

    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<Document> searchByName(String name) {
        return documentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Document> getByType(String type) {
        return documentRepository.findByTypeContainingIgnoreCase(type);
    }

    public Document updateDocument(Long id, String newName) {
        Document document = getDocument(id);
        document.setName(newName);
        return documentRepository.save(document);
    }

    public boolean deleteDocument(Long id) {
        try {
            Document document = getDocument(id);

            // Supprime le fichier physique si il existe
            if (useFilesystem && document.getFilePath() != null) {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            }

            // Supprime de la BDD
            documentRepository.delete(document);
            return true;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete document", ex);
        }
    }

    public Resource loadFileAsResource(Long documentId) {
        Document document = getDocument(documentId);

        if (useFilesystem && document.getFilePath() != null) {
            // Charge depuis le filesystem
            return loadFileAsResourceFromPath(document.getFilePath());
        } else {
            // Charge depuis la BDD (il faudrait implémenter ByteArrayResource)
            throw new RuntimeException("File loading from database content not implemented yet");
        }
    }

    private Resource loadFileAsResourceFromPath(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new FileSystemResource(path.toFile());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + filePath, ex);
        }
    }
}