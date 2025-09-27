package ma.nexotek.HireCraft.service.ai;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import ai.onnxruntime.OrtSession.SessionOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

/**
 * EmbeddingService
 * - Charge un modèle ONNX type BERT / Sentence-BERT
 * - Tokenize via DJL HuggingFaceTokenizer (0.30.0)
 * - Produit des embeddings pour CV & Job Offer
 * - Calcule la similarité cosinus & score 0..100
 */
@Service
@Slf4j
public class EmbeddingService {

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    @Value("${cv.scoring.model.path:models/resume-job-matcher-all-MiniLM-L6-v2.onnx}")
    private String modelPath;

    @Value("${cv.scoring.tokenizer.path:models/resume-job-matcher-all-MiniLM-L6-v2-tokenizer.json}")
    private String tokenizerPath;

    @Value("${cv.scoring.max.length:512}")
    private int maxLength;

    /** dimension détectée (fallback 384) */
    private int embeddingDim = 384;

    private boolean initialized = false;

    // --- Lifecycle ----------------------------------------------------------

    @PostConstruct
    public void init() {
        try {
            log.info("🚀 Initialisation EmbeddingService (DJL 0.30.0 + ONNX Runtime 1.19.2)...");
            initializeOnnxRuntime();
            initializeTokenizer();
            detectEmbeddingDim();
            this.initialized = true;
            log.info("✅ EmbeddingService initialisé (embeddingDim={})", embeddingDim);
        } catch (Exception e) {
            log.error("❌ Erreur init EmbeddingService: {}", e.getMessage(), e);
            this.initialized = false;
            throw new RuntimeException("Impossible d'initialiser EmbeddingService", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
                log.info("🧹 Session ONNX fermée");
            }
            if (env != null) {
                env.close();
                log.info("🧹 Environnement ONNX fermé");
            }
        } catch (Exception e) {
            log.error("⚠️ Erreur nettoyage: {}", e.getMessage(), e);
        }
    }

    // --- Init helpers -------------------------------------------------------

    private void initializeOnnxRuntime() throws OrtException, IOException {
        log.info("🔧 ONNX Runtime 1.19.2...");
        this.env = OrtEnvironment.getEnvironment();

        ClassPathResource modelResource = new ClassPathResource(modelPath);
        if (!modelResource.exists()) {
            throw new IOException("Modèle ONNX non trouvé dans le classpath: " + modelPath);
        }

        SessionOptions sessionOptions = new SessionOptions();
        sessionOptions.setOptimizationLevel(SessionOptions.OptLevel.ALL_OPT);
        sessionOptions.setIntraOpNumThreads(2);

        // CORRECTION: Utiliser InputStream au lieu de getFile()
        try (InputStream inputStream = modelResource.getInputStream()) {
            byte[] modelBytes = inputStream.readAllBytes();
            this.session = env.createSession(modelBytes, sessionOptions);
            log.info("✅ Modèle ONNX chargé: {}", modelPath);
            log.debug("Inputs: {} | Outputs: {}", session.getInputNames(), session.getOutputNames());
        }
    }

   private void initializeTokenizer() throws IOException {
    log.info("🔤 Chargement du tokenizer fine-tuné: {}...", tokenizerPath);
    try {
        ClassPathResource tokenizerResource = new ClassPathResource(tokenizerPath);
        if (!tokenizerResource.exists()) {
            throw new IOException("Tokenizer fine-tuné non trouvé: " + tokenizerPath);
        }
        
        //  TOKENIZER FINE-TUNÉ
        this.tokenizer = HuggingFaceTokenizer.builder()
                .optTokenizerPath(Paths.get(tokenizerResource.getURI()))
                .optPadToMaxLength()  // Option important pour le padding
                .build();
                
        log.info("✅ Tokenizer fine-tuné chargé: {}", tokenizerPath);
        
    } catch (Exception e) {
        log.error("❌ Erreur chargement tokenizer fine-tuné: {}", e.getMessage());
        // Fallback vers le tokenizer générique si nécessaire
        log.warn("🔄 Tentative de fallback vers bert-base-uncased...");
        this.tokenizer = HuggingFaceTokenizer.builder()
                .optTokenizerName("bert-base-uncased")
                .build();
        log.info("✅ Tokenizer de fallback chargé");
    }
}

    private void detectEmbeddingDim() {
        try {
            // Force la dimension pour resume-job-matcher-all-MiniLM-L6-v2
            if (modelPath.contains("resume-job-matcher-all-MiniLM-L6-v2")) {
                this.embeddingDim = 384;
                log.info("📐 embeddingDim forcée pour resume-job-matcher-all-MiniLM-L6-v2: {}", embeddingDim);
                return;
            }
            Map<String, NodeInfo> outputs = session.getOutputInfo();
            // Stratégie:
            // - Si "pooler_output" existe: shape [1, H] → H = embeddingDim
            // - Sinon "last_hidden_state": shape [1, T, H] → H = embeddingDim
            // - Sinon on essaie de déduire depuis le premier output
            Set<String> outNames = session.getOutputNames();

            if (outNames.contains("pooler_output")) {
                TensorInfo ti = (TensorInfo) outputs.get("pooler_output").getInfo();
                long[] shape = ti.getShape();
                if (shape.length >= 2) {
                    embeddingDim = (int) shape[shape.length - 1];
                    log.info("📐 embeddingDim détectée via pooler_output: {}", embeddingDim);
                    return;
                }
            }
            if (outNames.contains("last_hidden_state")) {
                TensorInfo ti = (TensorInfo) outputs.get("last_hidden_state").getInfo();
                long[] shape = ti.getShape(); // [1, T, H]
                if (shape.length >= 3) {
                    embeddingDim = (int) shape[shape.length - 1];
                    log.info("📐 embeddingDim détectée via last_hidden_state: {}", embeddingDim);
                    return;
                }
            }
            // Fallback: premier output
            for (String name : outNames) {
                TensorInfo ti = (TensorInfo) outputs.get(name).getInfo();
                if (ti != null && ti.getShape() != null) {
                    long[] shape = ti.getShape();
                    if (shape.length >= 2) {
                        embeddingDim = (int) shape[shape.length - 1];
                        log.info("📐 embeddingDim détectée via {}: {}", name, embeddingDim);
                        return;
                    }
                }
            }
            log.warn("⚠️ Impossible de détecter embeddingDim → fallback {}", embeddingDim);
        } catch (Exception e) {
            log.warn("⚠️ Erreur détection embeddingDim: {} → fallback {}", e.getMessage(), embeddingDim);
        }
    }

    // --- Public API ---------------------------------------------------------

    /** Embedding "sémantique" d'un texte générique. */
    public float[] embedText(String text) {
        ensureInitialized();
        String normalized = normalizeText(text);
        if (normalized.isEmpty()) {
            log.warn("Texte vide après normalisation → embedding nul");
            return new float[embeddingDim];
        }

        OnnxTensor inputTensor = null;
        OnnxTensor maskTensor = null;
        OnnxTensor tokenTypeTensor = null;
        OrtSession.Result result = null;

        try {
            // 1) Tokenisation
            Encoding encoding = tokenizer.encode(normalized);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();

            // 2) Troncature
            if (inputIds.length > maxLength) {
                inputIds = Arrays.copyOf(inputIds, maxLength);
                attentionMask = Arrays.copyOf(attentionMask, maxLength);
            }

            // 3) Padding
            if (inputIds.length < maxLength) {
                inputIds = padArray(inputIds, maxLength);
                attentionMask = padArray(attentionMask, maxLength);
            }

            // 4) Tenseurs (batch size = 1)
            long[][] inputIds2D = new long[][]{inputIds};
            long[][] attentionMask2D = new long[][]{attentionMask};
            long[][] tokenTypeIds2D = new long[1][inputIds.length]; // zéros par défaut

            inputTensor = OnnxTensor.createTensor(env, inputIds2D);
            maskTensor = OnnxTensor.createTensor(env, attentionMask2D);
            tokenTypeTensor = OnnxTensor.createTensor(env, tokenTypeIds2D);

            Map<String, OnnxTensor> inputs = Map.of(
                    "input_ids", inputTensor,
                    "attention_mask", maskTensor,
                    "token_type_ids", tokenTypeTensor
            );

            // 5) Inférence
            result = session.run(inputs);

            // 6) Lecture sortie(s)
            Set<String> outNames = session.getOutputNames();

            // a) Si pooler_output dispo → [1, H]
            if (outNames.contains("pooler_output")) {
                Optional<OnnxValue> valOpt = result.get("pooler_output");
                if (valOpt.isPresent()) {
                    Object val = valOpt.get().getValue(); // <-- OK maintenant
                    float[] pooled = extract1xH(val);
                    if (pooled != null) return pooled;
                }
            }

            // b) Sinon last_hidden_state → [1, T, H] → mean pooling sur tokens valides
            if (outNames.contains("last_hidden_state")) {
                Optional<OnnxValue> valOpt = result.get("last_hidden_state");
                if (valOpt.isPresent()) {
                    Object val = valOpt.get().getValue();
                    float[] pooled = meanPoolFrom1xTxH(val, attentionMask);
                    if (pooled != null) return pooled;
                }
            }

            // c) Fallback: premier output
            OnnxValue first = result.get(0);
            Object value = first.getValue();

            // [1, H]
            float[] oneByH = extract1xH(value);
            if (oneByH != null) return oneByH;

            // [1, T, H]
            float[] pooled = meanPoolFrom1xTxH(value, attentionMask);
            if (pooled != null) return pooled;

            log.warn("⚠️ Format de sortie ONNX inattendu ({}) → retour vecteur nul", value.getClass());
            return new float[embeddingDim];

        } catch (OrtException e) {
            log.error("Erreur ONNX: {}", e.getMessage(), e);
            return new float[embeddingDim];
        } catch (Exception e) {
            log.error("Erreur embedding: {}", e.getMessage(), e);
            return new float[embeddingDim];
        } finally {
            tryClose(result);
            tryClose(tokenTypeTensor);
            tryClose(maskTensor);
            tryClose(inputTensor);
        }
    }

    /** Wrappers explicites pour clarifier l'usage dans le workflow. */
    public float[] embedCV(String cvText) {
        return embedText(cvText);
    }

    public float[] embedJobOffer(String jobText) {
        return embedText(jobText);
    }

    /** Similarité cosinus entre deux embeddings. */
    public double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0 || v1.length != v2.length) {
            return 0.0;
        }
        double dot = 0.0, n1 = 0.0, n2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            n1 += v1[i] * v1[i];
            n2 += v2[i] * v2[i];
        }
        double denom = Math.sqrt(n1) * Math.sqrt(n2);
        return denom == 0.0 ? 0.0 : dot / denom;
    }

    /** Score de matching (0..100) entre CV et Offre. */
    public double scoreCvToJob(String cvText, String jobText) {
        float[] cv = embedCV(cvText);
        float[] job = embedJobOffer(jobText);
        return cosineSimilarity(cv, job) * 100.0;
    }

    /** Version "safe" qui remonte Optional pour gérer les erreurs en amont. */
    public Optional<float[]> safeEmbedText(String text) {
        try {
            return Optional.of(embedText(text));
        } catch (Exception e) {
            log.error("❌ safeEmbedText: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /** Batch (attention: embeddings calculés séquentiellement ici). */
    public List<float[]> embedTexts(List<String> texts) {
        if (!initialized || texts == null || texts.isEmpty()) return Collections.emptyList();
        List<float[]> out = new ArrayList<>(texts.size());
        for (String t : texts) out.add(embedText(t));
        return out;
    }

    /** Stats service (debug/health). */
    public Map<String, Object> getServiceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("initialized", initialized);
        stats.put("djl_version", "0.30.0");
        stats.put("onnx_version", "1.19.2");
        stats.put("model_path", modelPath);
        stats.put("tokenizer_path", tokenizerPath);
        stats.put("max_length", maxLength);
        stats.put("embedding_dim", embeddingDim);
        if (session != null) {
            try {
                stats.put("input_names", session.getInputNames());
                stats.put("output_names", session.getOutputNames());
            } catch (Exception e) {
                stats.put("session_error", e.getMessage());
            }
        }
        return stats;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // --- Private helpers ----------------------------------------------------

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("EmbeddingService non initialisé");
        }
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String cleaned = text
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replaceAll("[\r\n]+", " ")
                .replaceAll("\t+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        // Optionnel: tronquer pour éviter des entrées énormes (le tokeniseur gère aussi)
        if (cleaned.length() > 20000) {
            cleaned = cleaned.substring(0, 20000) + "...";
        }
        return cleaned;
    }

    private long[] padArray(long[] array, int targetLength) {
        long[] padded = new long[targetLength];
        System.arraycopy(array, 0, padded, 0, Math.min(array.length, targetLength));
        return padded;
    }

    /** Essaye d'extraire un vecteur [H] depuis une sortie [1, H]. */
    private float[] extract1xH(Object value) {
        try {
            if (value instanceof float[][] arr && arr.length >= 1) {
                // [1, H]
                return arr[0].length == embeddingDim ? arr[0] : Arrays.copyOf(arr[0], arr[0].length);
            }
            if (value instanceof float[] arr1d && arr1d.length == embeddingDim) {
                // [H]
                return arr1d;
            }
        } catch (Exception e) {
            log.warn("extract1xH: {}", e.getMessage());
        }
        return null;
    }

    /** Mean pooling sur tokens valides à partir d'une sortie [1, T, H]. */
    private float[] meanPoolFrom1xTxH(Object value, long[] attentionMask) {
        try {
            if (value instanceof float[][][] arr && arr.length >= 1) {
                // arr[0] = [T, H]
                return meanPooling(arr[0], attentionMask);
            }
        } catch (Exception e) {
            log.warn("meanPoolFrom1xTxH: {}", e.getMessage());
        }
        return null;
    }

    /** Mean pooling amélioré avec attentionMask. */
    private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
        if (tokenEmbeddings == null || tokenEmbeddings.length == 0) {
            return new float[embeddingDim];
        }
        int dim = tokenEmbeddings[0].length;
        float[] pooled = new float[dim];
        int valid = 0;

        int len = Math.min(tokenEmbeddings.length, attentionMask.length);
        for (int i = 0; i < len; i++) {
            if (attentionMask[i] == 1) {
                float[] tok = tokenEmbeddings[i];
                for (int j = 0; j < dim; j++) pooled[j] += tok[j];
                valid++;
            }
        }
        if (valid > 0) {
            for (int j = 0; j < dim; j++) pooled[j] /= valid;
        }
        return pooled;
    }

    private void tryClose(AutoCloseable c) {
        if (c == null) return;
        try { c.close(); } catch (Exception ignore) {}
    }
}