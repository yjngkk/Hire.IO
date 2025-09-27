package ma.nexotek.HireCraft.service.ai;

import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.model.Candidat;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@Slf4j
public class CVTextExtractionService {
    
    private final Tika tika;
    
    public CVTextExtractionService() {
        this.tika = new Tika();
        // Configuration de Tika pour éviter les problèmes de mémoire
        this.tika.setMaxStringLength(50000); // Max 50k caractères
    }
    
    /**
     * Extrait le texte d'un CV depuis un candidat
     */
    @Transactional(readOnly = true)
    public String extractCVText(Candidat candidat) {
        if (candidat.getCv() == null) {
            throw new IllegalArgumentException("Le candidat n'a pas de CV associé");
        }
        
        return extractTextFromDocument(candidat.getCv());
    }
    
    /**
     * Extrait le texte d'un document avec Apache Tika
     */
    public String extractTextFromDocument(Document document) {
        try {
            if (document.getContent() == null || document.getContent().length == 0) {
                throw new IllegalArgumentException("Le document est vide");
            }
            
            // Validation du type de fichier
            validateFileType(document);
            
            // Extraction avec Tika
            ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getContent());
            String rawText = tika.parseToString(inputStream);
            
            // Nettoyage et normalisation du texte
            String cleanedText = cleanAndNormalizeText(rawText);
            
            log.info(" Texte extrait avec succès du CV: {} caractères", cleanedText.length());
            
            return cleanedText;
            
        } catch (IOException e) {
            log.error("Erreur IO lors de l'extraction du texte: {}", e.getMessage());
            throw new RuntimeException("Erreur de lecture du document: " + e.getMessage(), e);
            
        } catch (TikaException e) { // ← CORRECTION PRINCIPALE
            log.error("Erreur Tika lors de l'extraction du texte: {}", e.getMessage());
            throw new RuntimeException("Impossible de parser le document: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error(" Erreur inattendue lors de l'extraction: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'extraction du texte", e);
        }
    }
    
    /**
     * Valide le type de fichier avant traitement
     */
    private void validateFileType(Document document) {
        String fileName = document.getName().toLowerCase();
        String fileType = document.getType();
        
        // Types supportés
        boolean isSupported = fileName.endsWith(".pdf") || 
                             fileName.endsWith(".doc") || 
                             fileName.endsWith(".docx") ||
                             fileName.endsWith(".txt") ||
                             (fileType != null && (
                                 fileType.contains("pdf") ||
                                 fileType.contains("word") ||
                                 fileType.contains("text")
                             ));
        
        if (!isSupported) {
            throw new IllegalArgumentException(
                "Type de fichier non supporté: " + fileName + " (type: " + fileType + ")"
            );
        }
        
        // Vérifier la taille (max 10MB)
        if (document.getSize() != null && document.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 10MB)");
        }
    }
    
    /**
     * Nettoie et normalise le texte extrait
     */
    private String cleanAndNormalizeText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Aucun texte exploitable trouvé dans le document");
        }
        
        String cleaned = rawText
            // Supprimer les caractères de contrôle
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
            // Normaliser les retours à la ligne
            .replaceAll("[\r\n]+", " ")
            // Normaliser les tabulations
            .replaceAll("\t+", " ")
            // Supprimer les espaces multiples
            .replaceAll("\\s+", " ")
            // Supprimer les caractères spéciaux problématiques
            .replaceAll("[\\u00A0\\u2007\\u202F]", " ") // Espaces non-sécables
            .trim();
        
        // Vérifier que le texte nettoyé n'est pas vide
        if (cleaned.length() < 10) {
            throw new IllegalArgumentException("Texte trop court après nettoyage (min 10 caractères)");
        }
        
        // Limiter la taille pour éviter les problèmes de mémoire
        if (cleaned.length() > 20000) {
            cleaned = cleaned.substring(0, 20000) + "...";
            log.warn("⚠️ Texte tronqué à 20k caractères");
        }
        
        return cleaned;
    }
    
    /**
     * Valide qu'un CV contient du texte exploitable
     */
    public boolean isValidCV(Candidat candidat) {
        try {
            String text = extractCVText(candidat);
            return text.length() > 50; // Minimum 50 caractères
        } catch (Exception e) {
            log.warn("⚠️ CV invalide pour le candidat {}: {}", candidat.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Méthode sécurisée pour tenter l'extraction
     */
    public ExtractionResult safeExtractText(Candidat candidat) {
        try {
            String text = extractCVText(candidat);
            CVTextStats stats = getTextStats(text);
            
            return ExtractionResult.builder()
                .success(true)
                .text(text)
                .stats(stats)
                .build();
                
        } catch (Exception e) {
            log.error("Échec extraction pour candidat {}: {}", candidat.getId(), e.getMessage());
            
            return ExtractionResult.builder()
                .success(false)
                .error("Erreur d'extraction: " + e.getMessage())
                .fallbackText("CV non lisible - Analyse manuelle requise")
                .build();
        }
    }
    
    /**
     * Extrait des statistiques basiques du CV
     */
    public CVTextStats getTextStats(String cvText) {
        if (cvText == null || cvText.trim().isEmpty()) {
            return CVTextStats.builder().build();
        }
        
        String[] words = cvText.split("\\s+");
        String[] sentences = cvText.split("[.!?]+");
        
        return CVTextStats.builder()
            .characterCount(cvText.length())
            .wordCount(words.length)
            .sentenceCount(Math.max(sentences.length, 1))
            .averageWordsPerSentence(sentences.length > 0 ? (double) words.length / sentences.length : 0)
            .build();
    }
    
    /**
     * Classe pour le résultat d'extraction
     */
    @lombok.Data
    @lombok.Builder
    public static class ExtractionResult {
        private boolean success;
        private String text;
        private String error;
        private String fallbackText;
        private CVTextStats stats;
    }
    
    /**
     * Classe pour les statistiques de texte
     */
    @lombok.Data
    @lombok.Builder
    public static class CVTextStats {
        private int characterCount;
        private int wordCount;
        private int sentenceCount;
        private double averageWordsPerSentence;
        
        public boolean isValidContent() {
            return wordCount > 10 && characterCount > 50;
        }
    }
}