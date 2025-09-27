package ma.nexotek.HireCraft.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ma.nexotek.HireCraft.model.ApiConfig;
import ma.nexotek.HireCraft.model.Form;
import ma.nexotek.HireCraft.dto.JobOfferRequest;
import ma.nexotek.HireCraft.repository.ApiConfigRepository;
import ma.nexotek.HireCraft.repository.FormRepository;
import ma.nexotek.HireCraft.util.ApplicationLinkUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobOfferGenerationService {

    private final RestTemplate restTemplate = new RestTemplate(); 
    private final ObjectMapper objectMapper;
    private final FormRepository jobOfferRepository;
    @Autowired
    private ApplicationLinkUtil applicationLinkUtil;
    private final ApiConfigRepository apiConfigRepository;

     @Value("${llm.api.openai.url}")
     private String openaiApiUrl;

    // @Value("${llm.api.openai.key}")
    // private String openaiApiKey;

     @Value("${llm.api.mistral.url}")
    private String mistralApiUrl;

    // @Value("${llm.api.mistral.key}")
    // private String mistralApiKey;

    @Value("${llm.api.groq.url}")
     private String groqApiUrl;

    // @Value("${llm.api.groq.key}")
    // private String groqApiKey;

    @Value("${llm.api.huggingface.url}")
    private String huggingfaceApiUrl;

    // @Value("${llm.api.huggingface.key}")
    // private String huggingfaceApiKey;

    @Value("${llm.api.default}")
    private String defaultModel;


    /**
     * Generate a job offer using the specified language model API
     */
    // Méthode pub
    public String generateJobOffer(JobOfferRequest jobOfferRequest) {
         log.info("Starting job offer generation for title: {}", jobOfferRequest.getTitle());
        
        // Test rapide pour vérifier ApplicationLinkUtil
        if (applicationLinkUtil == null) {
            log.error("ApplicationLinkUtil is null!");
            throw new RuntimeException("ApplicationLinkUtil not injected properly");
        }
        return generateJobOfferForTenant(jobOfferRequest);
    }

    // Méthode privée 
    private String generateJobOfferForTenant(JobOfferRequest jobOfferRequest) {
        String modelName = jobOfferRequest.getPreferredModel();

        if (modelName == null || modelName.isEmpty()) {
            modelName = defaultModel;
        }

        // RÉCUPÉRER LA CLÉ DEPUIS LA DB
        String apiKey = getApiKey(modelName);
        if (apiKey == null) {
            throw new RuntimeException("Aucune clé API configurée pour: " + modelName);
        }

        String prompt = createPrompt(jobOfferRequest);

         String generatedContent = switch (modelName.toLowerCase()) {
            case "openai" -> callOpenAIApi(prompt, apiKey);           
            case "mistral" -> callMistralApi(prompt, apiKey);
            case "groq" -> callGroqApi(prompt, apiKey);
            case "huggingface" -> callHuggingFaceApi(prompt, apiKey);
            default -> throw new IllegalArgumentException("Unsupported model: " + modelName);
        };

     

       // Save to database
      try {

        Form jobOffer = new Form(
                jobOfferRequest.getTitle(),
                jobOfferRequest.getMissions(),
                jobOfferRequest.getLocation(),
                jobOfferRequest.getContractType(),
                jobOfferRequest.getLevel(),
                jobOfferRequest.getSkills(),
                jobOfferRequest.getTone()
        );


           jobOffer.setTargetPlatform(jobOfferRequest.getTargetPlatform());
           jobOffer.setModelUsed(modelName);
           jobOffer.setGeneratedContent(generatedContent);
           jobOffer.setCategorie(jobOfferRequest.getCategorie());



           Form savedJobOffer = jobOfferRepository.save(jobOffer);
            log.info("Job offer saved with ID: {}", savedJobOffer.getId());
           
            String applicationLink = applicationLinkUtil.generateApplicationLink(savedJobOffer.getId());
            log.info("Generated application link: {}", applicationLink);
           
            // Concaténer le contenu généré avec le lien de candidature
            String finalContent = generatedContent + "\n\n" +
                "Candidater en ligne:" +
                applicationLink + "\n\n" +
                "Nous reviendrons vers vous rapidement.";

            // Mettre à jour avec le contenu final et le lien
            savedJobOffer.setGeneratedContent(finalContent);
            savedJobOffer.setApplicationLink(applicationLink);
            jobOfferRepository.save(savedJobOffer);

            log.info("Saved job offer to database with ID: {} and application link", savedJobOffer.getId());
            
            return finalContent;
            
        } catch (Exception e) {
            log.error("Error saving job offer to database", e);
            return generatedContent; // Retourner au moins le contenu généré en cas d'erreur
        }
    }


    /**
     * Create a prompt for the language model based on the job offer request
     */
    private String createPrompt(JobOfferRequest request) {
        // Get platform-specific instructions if a target platform is specified
        String platformSpecificInstructions = getPlatformSpecificInstructions(request.getTargetPlatform());

        // Build the prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Génère une annonce d'emploi professionnelle en utilisant les informations suivantes:\n\n");
        promptBuilder.append("Titre du poste: ").append(request.getTitle()).append("\n");
        promptBuilder.append("Missions principales: ").append(request.getMissions()).append("\n");
        promptBuilder.append("Lieu: ").append(request.getLocation()).append("\n");
        promptBuilder.append("Type de contrat: ").append(request.getContractType()).append("\n");
        promptBuilder.append("Niveau d'expérience: ").append(request.getLevel()).append("\n");
        promptBuilder.append("Compétences clés: ").append(request.getSkills()).append("\n");
        promptBuilder.append("Ton de l'annonce: ").append(request.getTone()).append("\n\n");

        // Add platform-specific instructions if available
        if (!platformSpecificInstructions.isEmpty()) {
            promptBuilder.append(platformSpecificInstructions).append("\n\n");
        }

        // Add general instructions for the job offer structure
        promptBuilder.append("L'annonce doit inclure:\n");
        promptBuilder.append("1. Un titre accrocheur\n");
        promptBuilder.append("2. Une introduction engageante sur l'entreprise\n");
        promptBuilder.append("3. Une description détaillée du poste\n");
        promptBuilder.append("4. Les responsabilités principales\n");
        promptBuilder.append("5. Les compétences et qualifications requises\n");
        promptBuilder.append("6. Les avantages et bénéfices offerts\n");
        promptBuilder.append("7. Une conclusion avec un appel à l'action clair\n\n");
        
        promptBuilder.append("Utilise un ton ").append(request.getTone());
        promptBuilder.append(" et assure-toi que l'annonce soit optimisée pour les algorithmes de recherche d'emploi.");
        promptBuilder.append("IMPORTANT: Rédige une annonce concise et impactante, évite les répétitions et privilégie la clarté. Maximum 800-1000 mots, paragraphes courts, informations essentielles uniquement.\n\n");


        return promptBuilder.toString();
    }

    /**
     * Get platform-specific instructions based on the target platform
     */
    private String getPlatformSpecificInstructions(String platform) {
        if (platform == null || platform.isEmpty()) {
            return "";
        }

        Map<String, String> instructions = new HashMap<>();
        
        // LinkedIn-specific instructions
        instructions.put("linkedin", 
            "L'annonce doit être optimisée pour LinkedIn:\n" +
            "- Longueur idéale entre 1500-2000 caractères\n" +
            "- Utiliser des mots-clés pertinents au secteur\n" +
            "- Structure claire avec des paragraphes courts\n" +
            "- Ton professionnel mais engageant\n" +
            "- Éviter le jargon trop technique\n" +
            "- Inclure 3-5 hashtags pertinents à la fin");
        
        // Indeed-specific instructions
        instructions.put("indeed", 
            "L'annonce doit être optimisée pour Indeed:\n" +
            "- Structure très claire avec des titres de section\n" +
            "- Utiliser des listes à puces pour les responsabilités et compétences\n" +
            "- Inclure des informations sur le salaire si disponible\n" +
            "- Mentionner les avantages sociaux de façon détaillée\n" +
            "- Utiliser des mots-clés spécifiques au poste que les candidats pourraient rechercher");
        
        // Monster-specific instructions
        instructions.put("monster", 
            "L'annonce doit être optimisée pour Monster:\n" +
            "- Utiliser un format clair et direct\n" +
            "- Mettre l'accent sur la culture d'entreprise\n" +
            "- Inclure des détails sur l'environnement de travail\n" +
            "- Spécifier clairement les qualifications minimales vs. préférées\n" +
            "- Inclure des informations sur le processus de candidature");

        return instructions.getOrDefault(platform.toLowerCase(), "");
    }

    /**
     * NOUVELLE MÉTHODE SIMPLE pour récupérer la clé API
     */
    private String getApiKey(String modelName) {
        // D'abord chercher par nom de provider
        Optional<ApiConfig> config = apiConfigRepository.findByProviderName(modelName);
        if (config.isPresent()) {
            return config.get().getApiKey();
        }
        
        // Sinon prendre le modèle actif
        Optional<ApiConfig> activeConfig = apiConfigRepository.findByIsActiveTrue();
        if (activeConfig.isPresent()) {
            log.info("Utilisation du modèle actif: {}", activeConfig.get().getProviderName());
            return activeConfig.get().getApiKey();
        }
        
        return null;
    }
    /**
     * Call the Mistral API to generate a job offer
     */
    private String callMistralApi(String prompt, String apiKey) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "mistral-small-latest");
            requestBody.put("temperature", 0.5);
            requestBody.put("max_tokens", 1500);
            
            ArrayNode messagesArray = requestBody.putArray("messages");
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", "user");
            messageNode.put("content", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(mistralApiUrl, entity, String.class);
            return extractContentFromResponse(response.getBody());
        } catch (RestClientException e) {
            log.error("Error calling Mistral API: {}", e.getMessage());
            throw new RuntimeException("Error calling Mistral API: " + e.getMessage(), e);
        }
    }

    private String callOpenAIApi(String prompt, String apiKey) {
    try {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-4o");
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.7);
        
        ArrayNode messagesArray = requestBody.putArray("messages");
        
        // Message système pour optimiser les résultats
        ObjectNode systemMessage = messagesArray.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un expert en recrutement et rédaction d'offres d'emploi professionnelles. " +
                                   "Crée des annonces engageantes, claires et optimisées pour attirer les meilleurs candidats.");
        
        // Message utilisateur
        ObjectNode userMessage = messagesArray.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Configuration timeout optimisée pour OpenAI
        RestTemplate openaiRestTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10 secondes
        factory.setReadTimeout(45000);     // 45 secondes
        openaiRestTemplate.setRequestFactory(factory);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        log.info("Calling OpenAI API");
        ResponseEntity<String> response = openaiRestTemplate.postForEntity(openaiApiUrl, entity, String.class);
        
        return extractOpenAIResponse(response.getBody());
        
    } catch (RestClientException e) {
        log.error("OpenAI API error: {}", e.getMessage());
        throw new RuntimeException("Erreur OpenAI API: " + e.getMessage(), e);
    }
}

// 4. EXTRACTION RÉPONSE OPENAI
private String extractOpenAIResponse(String responseBody) {
    try {
        log.info("OpenAI response received");
        
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        if (responseJson.has("choices") && responseJson.path("choices").isArray()) {
            JsonNode choice = responseJson.path("choices").get(0);
            
            if (choice.has("message")) {
                String content = choice.path("message").path("content").asText();
                
                if (content == null || content.trim().isEmpty()) {
                    log.error("Empty OpenAI response: {}", responseBody);
                    return "Erreur: Réponse vide d'OpenAI";
                }
                
                return content;
            }
        }
        
        // Gestion des erreurs OpenAI
        if (responseJson.has("error")) {
            String errorMessage = responseJson.path("error").path("message").asText();
            String errorType = responseJson.path("error").path("type").asText();
            throw new RuntimeException("Erreur OpenAI [" + errorType + "]: " + errorMessage);
        }
        
        return "Erreur: format de réponse OpenAI inattendu";
        
    } catch (Exception e) {
        log.error("Error extracting OpenAI response", e);
        throw new RuntimeException("Erreur d'analyse OpenAI: " + e.getMessage());
    }
}

    /**
     * Call the Grok API to generate a job offer
     */
    private String callGroqApi(String prompt, String apiKey) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "llama-3.1-8b-instant");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1500);
            
            ArrayNode messagesArray = requestBody.putArray("messages");
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", "user");
            messageNode.put("content", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(groqApiUrl, entity, String.class);
            return extractContentFromResponse(response.getBody());
        } catch (RestClientException e) {
            log.error("Error calling Grok API: {}", e.getMessage());
            throw new RuntimeException("Error calling Grok API: " + e.getMessage(), e);
        }
    }

   private String callHuggingFaceApi(String prompt, String apiKey) {
    try {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("inputs", prompt);
        
        ObjectNode parameters = requestBody.putObject("parameters");
        parameters.put("max_new_tokens", 500); // Réduit pour éviter timeouts
        parameters.put("temperature", 0.7);
        parameters.put("do_sample", true);
        parameters.put("return_full_text", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        log.info("Calling HuggingFace API...");
        ResponseEntity<String> response = restTemplate.postForEntity(huggingfaceApiUrl, entity, String.class);
        
        return extractHuggingFaceResponse(response.getBody());
        
    } catch (HttpClientErrorException e) {
        String errorBody = e.getResponseBodyAsString();
        log.error("HuggingFace API error: {} - {}", e.getStatusCode(), errorBody);
        
        if (e.getStatusCode().value() == 503) {
            throw new RuntimeException("Le modèle HuggingFace est en cours de chargement. Réessayez dans 30 secondes.");
        } else if (e.getStatusCode().value() == 401) {
            throw new RuntimeException("Token HuggingFace invalide. Vérifiez votre clé API.");
        }
        
        throw new RuntimeException("Erreur HuggingFace API: " + errorBody);
    } catch (RestClientException e) {
        log.error("Error calling HuggingFace API: {}", e.getMessage());
        throw new RuntimeException("Erreur HuggingFace API: " + e.getMessage());
    }
}

private String extractHuggingFaceResponse(String responseBody) {
    try {
        log.info("HuggingFace response: {}", responseBody);
        
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        if (responseJson.isArray() && responseJson.size() > 0) {
            String generatedText = responseJson.get(0).path("generated_text").asText();
            
            if (generatedText == null || generatedText.trim().isEmpty()) {
                return "Le modèle n'a pas généré de contenu. Réessayez.";
            }
            
            return generatedText;
        }
        
        // Gestion des erreurs dans la réponse
        if (responseJson.has("error")) {
            String error = responseJson.path("error").asText();
            throw new RuntimeException("Erreur HuggingFace: " + error);
        }
        
        return "Erreur: format de réponse inattendu - " + responseBody;
        
    } catch (Exception e) {
        log.error("Error extracting HuggingFace response", e);
        throw new RuntimeException("Error parsing HuggingFace response: " + e.getMessage());
    }
}

    /**
     * Extract the content from the API response
     */
    private String extractContentFromResponse(String response) {
    try {
        ObjectMapper mapper = new ObjectMapper();

        // Cas Hugging Face (souvent un array de JSON)
        if (response.trim().startsWith("[")) {
            JsonNode array = mapper.readTree(response);
            if (array.isArray() && array.size() > 0) {
                JsonNode first = array.get(0);
                if (first.has("generated_text")) {
                    return first.path("generated_text").asText();
                }
            }
        }

        // Cas normal (Mistral, Groq, autres)
        JsonNode root = mapper.readTree(response);

        // Cas Mistral / OpenAI-like
        if (root.has("choices") && root.path("choices").isArray()) {
            JsonNode choice = root.path("choices").get(0);

            // Mistral / OpenAI → message.content
            if (choice.has("message")) {
                return choice.path("message").path("content").asText();
            }

            // Groq → text
            if (choice.has("text")) {
                return choice.path("text").asText();
            }
        }

        // Cas Hugging Face (objet simple)
        if (root.has("generated_text")) {
            return root.path("generated_text").asText();
        }

        return "Erreur: format de réponse inattendu";

    } catch (Exception e) {
        e.printStackTrace();
        return "Erreur d'analyse JSON: " + e.getMessage();
    }
}


/**
 * Generate job offer with automatic fallback to other models
 */
public String generateJobOfferWithFallback(JobOfferRequest jobOfferRequest) {
    try {
        // Essayer d'abord le modèle préféré
        return generateJobOfferForTenant(jobOfferRequest);
    } catch (Exception error) {
        log.warn("Primary model failed trying fallback: {}", error.getMessage());
        
        // Sauvegarder le modèle original
        String originalModel = jobOfferRequest.getPreferredModel();
        
        try {
            // Essayer DeepSeek comme fallback
            jobOfferRequest.setPreferredModel("huggingface");
            return generateJobOfferForTenant(jobOfferRequest);
        } catch (Exception error2) {
            log.warn("DeepSeek failed trying Groq: {}",  error2.getMessage());
            
            try {
                // Essayer Grok comme dernier recours
                jobOfferRequest.setPreferredModel("groq");
                return generateJobOfferForTenant(jobOfferRequest);
            } catch (Exception error3) {
                log.error("All models failed");
                throw new RuntimeException("Tous les modèles AI sont indisponibles");
            } finally {
                // Restaurer le modèle original
                jobOfferRequest.setPreferredModel(originalModel);
            }
        }
    }
}

/**
     * Get all job offers
     */
    public List<Form> getAllJobOffers() {
        return jobOfferRepository.findAll();
    }

    /**
     * Get job offer by ID
     */
    public Optional<Form> getJobOfferById(Long id) {
        return jobOfferRepository.findById(id);
    }

    /**
     * Delete job offer
     */
    public boolean deleteJobOffer(Long id) {
        if (jobOfferRepository.existsById(id)) {
            jobOfferRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Search job offers by title
     */
    public List<Form> searchJobOffers(String title) {
        return jobOfferRepository.findByTitleContaining(title);
    }

}