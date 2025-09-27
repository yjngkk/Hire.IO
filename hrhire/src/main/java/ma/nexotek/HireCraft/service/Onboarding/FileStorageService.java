package ma.nexotek.HireCraft.service.Onboarding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir:}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        // Ne crée pas de dossier si uploadDir est vide
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            this.fileStorageLocation = null;
            return;
        }

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create directory", ex);
        }
    }

    public String storeFile(MultipartFile file, String customFileName) {
        // Initialize storage location if not done
        if (this.fileStorageLocation == null) {
            init();
        }

        // Normalize file name
        String fileName = StringUtils.cleanPath(customFileName);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Create unique filename to avoid conflicts
            String fileExtension = "";
            if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            if (this.fileStorageLocation == null) {
                init();
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            if (this.fileStorageLocation == null) {
                init();
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    public boolean fileExists(String fileName) {
        try {
            if (this.fileStorageLocation == null) {
                init();
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.exists(filePath);
        } catch (Exception ex) {
            return false;
        }
    }

    public long getFileSize(String fileName) {
        try {
            if (this.fileStorageLocation == null) {
                init();
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
            return 0;
        } catch (IOException ex) {
            return 0;
        }
    }

    public String getContentType(String fileName) {
        try {
            if (this.fileStorageLocation == null) {
                init();
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                return Files.probeContentType(filePath);
            }
            return "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    public Path getFileStorageLocation() {
        if (this.fileStorageLocation == null) {
            init();
        }
        return this.fileStorageLocation;
    }
}