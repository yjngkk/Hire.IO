package ma.nexotek.HireCraft.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.nexotek.HireCraft.model.Test;
import ma.nexotek.HireCraft.model.Exercise;
import ma.nexotek.HireCraft.model.Question;
import ma.nexotek.HireCraft.model.Answer;
import ma.nexotek.HireCraft.model.ApiConfig;
import ma.nexotek.HireCraft.dto.TestRequestDTO_AI;
import ma.nexotek.HireCraft.repository.TestRepository;
import ma.nexotek.HireCraft.repository.ExerciseRepository;
import ma.nexotek.HireCraft.repository.QuestionRepository;
import ma.nexotek.HireCraft.repository.AnswerRepository;
import ma.nexotek.HireCraft.repository.ApiConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * Service for generating QCM tests using AI language models )
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AITestGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final TestRepository testRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Value("${llm.api.mistral.url}")
    private String mistralApiUrl;

   
    @Value("${llm.api.groq.url}")
    private String groqApiUrl;

     @Value("${llm.api.huggingface.url}")
    private String huggingfaceApiUrl;

      @Value("${llm.api.openai.url}")
     private String openaiApiUrl;


    @Value("${llm.api.default}")
    private String defaultModel;

    private final ApiConfigRepository apiConfigRepository;

     private String getApiKey(String modelName) {
        Optional<ApiConfig> config = apiConfigRepository.findByProviderName(modelName);
        if (config.isPresent()) {
            return config.get().getApiKey();
        }
        
        Optional<ApiConfig> activeConfig = apiConfigRepository.findByIsActiveTrue();
        if (activeConfig.isPresent()) {
            return activeConfig.get().getApiKey();
        }
        
        throw new RuntimeException("Aucune clé API configurée pour: " + modelName);
    }

    /**
     * Private method to generate QCM with AI
     */
    private Test generateQcmWithAI(TestRequestDTO_AI qcmRequest) {
        String modelName = qcmRequest.getAiProvider();
        if (modelName == null || modelName.isEmpty()) {
            modelName = defaultModel;
        }
        String prompt = createPrompt(qcmRequest);

         String generatedContent = switch (modelName.toLowerCase()) {
            case "openai" -> callOpenAIApi(prompt);           
            case "mistral" -> callMistralApi(prompt);
            case "groq" -> callGroqApi(prompt);
            case "huggingface" -> callHuggingFaceApi(prompt);
            default -> throw new IllegalArgumentException("Unsupported model: " + modelName);
        };

        // Parse the AI response and create entities
        Test savedTest = parseAndSaveQcm(generatedContent, qcmRequest);
        
        log.info("Saved QCM test to database with ID: {}", savedTest.getId());
        return savedTest;
    }

    /**
     * Create a prompt for the language model based on the QCM request
     */
    private String createPrompt(TestRequestDTO_AI request) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("Génère un QCM (Questionnaire à Choix Multiple) en français avec les spécifications suivantes:\n\n");
        promptBuilder.append("Sujet: ").append(request.getSubject()).append("\n");
        promptBuilder.append("Domaine: ").append(request.getDomain()).append("\n");
        promptBuilder.append("Thème: ").append(request.getTheme()).append("\n");
        promptBuilder.append("Niveau de difficulté: ").append(request.getDifficulty()).append("\n");
        promptBuilder.append("Nombre de questions: ").append(request.getNumberOfQuestions()).append("\n\n");

        promptBuilder.append("IMPORTANT: Réponds UNIQUEMENT avec un JSON valide dans ce format exact:\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"title\": \"Titre du QCM\",\n");
        promptBuilder.append("  \"description\": \"Description du QCM\",\n");
        promptBuilder.append("  \"questions\": [\n");
        promptBuilder.append("    {\n");
        promptBuilder.append("      \"questionText\": \"Texte de la question\",\n");
        promptBuilder.append("      \"points\": 5,\n");
        promptBuilder.append("      \"answers\": [\n");
        promptBuilder.append("        {\"text\": \"Réponse A\", \"index\": 0},\n");
        promptBuilder.append("        {\"text\": \"Réponse B\", \"index\": 1},\n");
        promptBuilder.append("        {\"text\": \"Réponse C\", \"index\": 2},\n");
        promptBuilder.append("        {\"text\": \"Réponse D\", \"index\": 3}\n");
        promptBuilder.append("      ],\n");
        promptBuilder.append("      \"correctAnswer\": 0\n");
        promptBuilder.append("    }\n");
        promptBuilder.append("  ]\n");
        promptBuilder.append("}\n\n");

        promptBuilder.append("Règles importantes:\n");
        promptBuilder.append("- Chaque question doit avoir exactement 4 réponses (A, B, C, D)\n");
        promptBuilder.append("- Une seule bonne réponse par question (QCM à choix unique)\n");
        promptBuilder.append("- Les points par question peuvent varier selon la difficulté\n");
        promptBuilder.append("- Questions pertinentes et de qualité professionnelle\n");
        promptBuilder.append("- Pas de texte supplémentaire, uniquement le JSON\n");

        return promptBuilder.toString();
    }

    /**
     * Call the Mistral API to generate a QCM
     */
    private String callMistralApi(String prompt) {
        try {

            String apiKey = getApiKey("mistral"); // RÉCUPÉRER DEPUIS LA DB

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "mistral-large-latest");
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 3000);
            
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

    /**
     * Call the Groq API to generate a QCM
     */
    private String callGroqApi(String prompt) {
        try {
            String apiKey = getApiKey("groq"); // RÉCUPÉRER DEPUIS LA DB

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "llama3-8b-8192");
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 3000);
            
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
            log.error("Error calling Groq API: {}", e.getMessage());
            throw new RuntimeException("Error calling Groq API: " + e.getMessage(), e);
        }
    }
    /**
 * Call the OpenAI API to generate a QCM
 */
private String callOpenAIApi(String prompt) {
    try {
        String apiKey = getApiKey("openai"); // RÉCUPÉRER DEPUIS LA DB

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-4o");
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 3000);
        
        ArrayNode messagesArray = requestBody.putArray("messages");
        
        // Message système
        ObjectNode systemMessage = messagesArray.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un expert en création de QCM professionnels.");
        
        // Message utilisateur
        ObjectNode userMessage = messagesArray.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(openaiApiUrl, entity, String.class);
        return extractContentFromResponse(response.getBody());
    } catch (RestClientException e) {
        log.error("Error calling OpenAI API: {}", e.getMessage());
        throw new RuntimeException("Error calling OpenAI API: " + e.getMessage(), e);
    }
}

/**
 * Call the HuggingFace API to generate a QCM
 */
private String callHuggingFaceApi(String prompt) {
    try {
        String apiKey = getApiKey("huggingface"); // RÉCUPÉRER DEPUIS LA DB

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("inputs", prompt);
        
        ObjectNode parameters = requestBody.putObject("parameters");
        parameters.put("max_new_tokens", 1000);
        parameters.put("temperature", 0.3);
        parameters.put("do_sample", true);
        parameters.put("return_full_text", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(huggingfaceApiUrl, entity, String.class);
        
        // HuggingFace a un format de réponse différent
        return extractHuggingFaceResponse(response.getBody());
    } catch (RestClientException e) {
        log.error("Error calling HuggingFace API: {}", e.getMessage());
        throw new RuntimeException("Error calling HuggingFace API: " + e.getMessage(), e);
    }
}

/**
 * Extraire la réponse HuggingFace (format différent)
 */
private String extractHuggingFaceResponse(String responseBody) {
    try {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        if (responseJson.isArray() && responseJson.size() > 0) {
            return responseJson.get(0).path("generated_text").asText();
        }
        
        return responseBody; // Fallback
    } catch (Exception e) {
        log.error("Error extracting HuggingFace response", e);
        return responseBody;
    }
}

    /**
     * Extract the content from the API response
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            return responseJson.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error extracting content from response", e);
            throw new RuntimeException("Error parsing API response: " + e.getMessage(), e);
        }
    }

    /**
     * Parse AI response and save QCM to database
     */
     private Test parseAndSaveQcm(String aiResponse, TestRequestDTO_AI request) {
        try {
            String cleanedJson = extractJsonFromResponse(aiResponse);
            JsonNode qcmJson = objectMapper.readTree(cleanedJson);            
            
            // Create and save Exercise
            Exercise exercise = new Exercise();
            exercise.setTitle(qcmJson.path("title").asText());
            exercise.setType("QCM");
            exercise.setDomain(request.getDomain());
            exercise.setTheme(request.getTheme() != null ? request.getTheme() : "Général");
            exercise.setDifficulty(request.getDifficulty());
            exercise.setDuration(request.getTotalDuration());
            
            // Calculate total points
            JsonNode questionsNode = qcmJson.path("questions");
            int totalPoints = 0;
            for (JsonNode questionNode : questionsNode) {
                totalPoints += questionNode.path("points").asInt(5);
            }
            exercise.setTotalPoints(totalPoints);
            
            Exercise savedExercise = exerciseRepository.save(exercise);
            
            // Create and save Questions and Answers
            for (JsonNode questionNode : questionsNode) {
                Question question = new Question();
                question.setQuestionText(questionNode.path("questionText").asText());
                question.setPoints(questionNode.path("points").asInt(5));
                question.setCorrectAnswer(questionNode.path("correctAnswer").asInt(0));
                question.setExercise(savedExercise);
                
                Question savedQuestion = questionRepository.save(question);
                
                // Create and save Answers
                JsonNode answersNode = questionNode.path("answers");
                for (JsonNode answerNode : answersNode) {
                    Answer answer = new Answer();
                    answer.setAnswerText(answerNode.path("text").asText());
                    answer.setAnswerIndex(answerNode.path("index").asInt());
                    answer.setQuestion(savedQuestion);
                    
                    answerRepository.save(answer);
                }
            }
                        
            // Create and save Test
            Test test = new Test();
            test.setName(request.getName());
            test.setDescription(request.getDescription() != null ? request.getDescription() : qcmJson.path("description").asText());
            test.setDifficulty(request.getDifficulty());
            test.setTotalDuration(request.getTotalDuration());
            test.setTotalPoints(totalPoints);
            test.setStatus("Brouillon");
            test.setExercises(List.of(savedExercise));
            test.setCategorie("Développement");
            
            return testRepository.save(test);
            
        } catch (Exception e) {
            log.error("Error parsing and saving QCM: {}", e.getMessage());
            throw new RuntimeException("Error processing AI response: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QCM with automatic fallback to other models
     */
    public Test generateQcmWithFallback(TestRequestDTO_AI qcmRequest) {
    try {
        return generateQcmWithAI(qcmRequest);
    } catch (Exception error) {
        log.warn("Primary model failed, trying fallback: {}", error.getMessage());
        
        String originalModel = qcmRequest.getAiProvider();
        
        try {
            // Essayer OpenAI comme fallback
            qcmRequest.setAiProvider("openai");
            return generateQcmWithAI(qcmRequest);
        } catch (Exception error2) {
            log.warn("OpenAI failed, trying Groq: {}", error2.getMessage());
            
            try {
                // Essayer Groq
                qcmRequest.setAiProvider("groq");
                return generateQcmWithAI(qcmRequest);
            } catch (Exception error3) {
                log.warn("Groq failed, trying Mistral: {}", error3.getMessage());
                
                try {
                    // Dernier recours : Mistral
                    qcmRequest.setAiProvider("mistral");
                    return generateQcmWithAI(qcmRequest);
                } catch (Exception error4) {
                    log.error("All models failed");
                    throw new RuntimeException("Tous les modèles AI sont indisponibles");
                } finally {
                    qcmRequest.setAiProvider(originalModel);
                }
            }
        }
    }
}



    // ========== NOUVELLES MÉTHODES POUR GÉNÉRATION VIA MESSAGE ==========

/**
 * NOUVELLE FONCTIONNALITÉ - Générer QCM depuis message utilisateur
 */
public Test generateQcmFromUserMessage(String userMessage, String aiProvider) {
    TestRequestDTO_AI parsedRequest = analyzeUserRequest(userMessage, aiProvider);
    return generateQcmWithFallback(parsedRequest);
}

/**
 * Analyser la demande utilisateur avec l'IA
 */
private TestRequestDTO_AI analyzeUserRequest(String userMessage, String aiProvider) {
    String analysisPrompt = createUserAnalysisPrompt(userMessage);
    
    String aiResponse = switch (aiProvider.toLowerCase()) {
        case "mistral" -> callMistralApi(analysisPrompt);
        case "groq" -> callGroqApi(analysisPrompt);
        default -> callMistralApi(analysisPrompt);
    };
    
    return parseUserAnalysis(aiResponse, userMessage);
}

/**
 * Créer le prompt d'analyse de la demande utilisateur
 */
private String createUserAnalysisPrompt(String userMessage) {
    StringBuilder promptBuilder = new StringBuilder();
    
    promptBuilder.append("Analyse cette demande d'utilisateur et extrais les paramètres pour créer un QCM :\n\n");
    promptBuilder.append("Demande utilisateur: \"").append(userMessage).append("\"\n\n");
    
    promptBuilder.append("IMPORTANT: Réponds UNIQUEMENT avec un JSON dans ce format exact :\n");
    promptBuilder.append("{\n");
    promptBuilder.append("  \"name\": \"Nom du test généré automatiquement\",\n");
    promptBuilder.append("  \"description\": \"Description du QCM\",\n");
    promptBuilder.append("  \"subject\": \"Sujet principal\",\n");
    promptBuilder.append("  \"domain\": \"Domaine technique\",\n");
    promptBuilder.append("  \"theme\": \"Thème spécifique ou null\",\n");
    promptBuilder.append("  \"difficulty\": \"Facile|Moyen|Difficile\",\n");
    promptBuilder.append("  \"numberOfQuestions\": nombre_entier,\n");
    promptBuilder.append("  \"totalDuration\": durée_en_minutes\n");
    promptBuilder.append("}\n\n");
    
    promptBuilder.append("Règles d'analyse :\n");
    promptBuilder.append("- Si le nombre de questions n'est pas spécifié, utilise 5 par défaut\n");
    promptBuilder.append("- Si la difficulté n'est pas mentionnée, utilise 'Moyen'\n");
    promptBuilder.append("- Calcule une durée appropriée (2-3 minutes par question)\n");
    promptBuilder.append("- Déduis le domaine et le sujet du contexte\n");
    promptBuilder.append("- Génère un nom de test descriptif\n");
    promptBuilder.append("- Si le thème est trop général, mets null\n\n");
    
    promptBuilder.append("Exemples :\n");
    promptBuilder.append("'QCM Java débutant' → difficulty: 'Facile', domain: 'Java', subject: 'Programmation Java'\n");
    promptBuilder.append("'Test avancé Spring Boot' → difficulty: 'Difficile', domain: 'Spring', theme: 'Spring Boot'\n");
    promptBuilder.append("'Quiz rapide JavaScript' → numberOfQuestions: 5, domain: 'JavaScript'\n\n");
    
    promptBuilder.append("Pas de texte supplémentaire, uniquement le JSON.");
    
    return promptBuilder.toString();
}

/**
 * Parser l'analyse de la demande utilisateur
 */
private TestRequestDTO_AI parseUserAnalysis(String aiResponse, String originalMessage) {
    try {
        String cleanedJson = extractJsonFromResponse(aiResponse);
        JsonNode analysisJson = objectMapper.readTree(cleanedJson);
        
        TestRequestDTO_AI request = new TestRequestDTO_AI();
        request.setName(analysisJson.path("name").asText("QCM Généré"));
        request.setDescription(analysisJson.path("description").asText());
        request.setSubject(analysisJson.path("subject").asText());
        request.setDomain(analysisJson.path("domain").asText());
        
        String theme = analysisJson.path("theme").asText();
        request.setTheme("null".equals(theme) ? null : theme);
        
        request.setDifficulty(analysisJson.path("difficulty").asText("Moyen"));
        request.setNumberOfQuestions(analysisJson.path("numberOfQuestions").asInt(5));
        request.setTotalDuration(analysisJson.path("totalDuration").asInt(15));
        request.setAiProvider("mistral");
        
        log.info("Parsed user request: {}", request);
        return request;
        
    } catch (Exception e) {
        log.error("Error parsing user analysis: {}", e.getMessage());
        return createFallbackRequest(originalMessage);
    }
}

/**
 * Créer une requête par défaut en cas d'erreur
 */
private TestRequestDTO_AI createFallbackRequest(String originalMessage) {
    TestRequestDTO_AI request = new TestRequestDTO_AI();
    request.setName("QCM - " + originalMessage.substring(0, Math.min(50, originalMessage.length())));
    request.setDescription("QCM généré à partir de : " + originalMessage);
    request.setSubject("Général");
    request.setDomain("Informatique");
    request.setTheme(null);
    request.setDifficulty("Moyen");
    request.setNumberOfQuestions(5);
    request.setTotalDuration(15);
    request.setAiProvider("mistral");
    return request;
}


    /**
 * Extraire le JSON de la réponse IA (enlever les backticks, texte supplémentaire)
 */
/**
 * Extraire le JSON de la réponse IA (enlever les backticks, texte supplémentaire)
 */
private String extractJsonFromResponse(String response) {
    log.info("Raw AI response: {}", response); // Pour voir ce qu'on reçoit
    
    // Enlever les backticks markdown
    response = response.replaceAll("```json", "").replaceAll("```", "");
    
    // Chercher le premier { et le dernier }
    int firstBrace = response.indexOf('{');
    int lastBrace = response.lastIndexOf('}');
    
    if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
        String extracted = response.substring(firstBrace, lastBrace + 1);
        log.info("Extracted JSON: {}", extracted);
        return extracted;
    }
    
    // Si pas de JSON trouvé, retourner tel quel
    log.warn("No JSON found in response, returning as-is");
    return response.trim();
}
    /**
     * Get all tests
     */
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    /**
     * Get test by ID
     */
    public Optional<Test> getTestById(Long id) {
        return testRepository.findById(id);
    }

    /**
     * Delete test
     */
    public boolean deleteTest(Long id) {
        if (testRepository.existsById(id)) {
            testRepository.deleteById(id);
            return true;
        }
        return false;
    }
}