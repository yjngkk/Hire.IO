package ma.nexotek.HireCraft.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class ApplicationLinkUtil {

    @Value("${app.encryption.secret-key:mySecretKey123456}")
    private String secretKey;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * Génère une clé AES de 128 bits à partir de la clé secrète
     */
    private SecretKeySpec generateKey() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            // Prendre seulement les 16 premiers octets pour AES-128
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de la clé", e);
        }
    }

    /**
     * Crypte l'ID de l'offre
     */
    public String encryptId(Long id) {
        try {
            SecretKeySpec keySpec = generateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encryptedBytes = cipher.doFinal(id.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du cryptage de l'ID: " + e.getMessage(), e);
        }
    }

    /**
     * Décrypte l'ID de l'offre
     */
    public Long decryptId(String encryptedId) {
        try {
            SecretKeySpec keySpec = generateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decodedBytes = Base64.getUrlDecoder().decode(encryptedId);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return Long.parseLong(new String(decryptedBytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du décryptage de l'ID: " + e.getMessage(), e);
        }
    }

    /**
     * Génère le lien d'application complet avec l'ID crypté
     */
    public String generateApplicationLink(Long formId) {
        String encryptedId = encryptId(formId);
        return baseUrl + "/create-candidature/" + encryptedId;
    }

    /**
     * Extrait l'ID de l'offre depuis un lien d'application
     */
    public Long extractFormIdFromLink(String applicationLink) {
        String encryptedId = applicationLink.substring(applicationLink.lastIndexOf("/") + 1);
        return decryptId(encryptedId);
    }
}