package ma.nexotek.HireCraft.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LinkedInService {
    private static final Logger logger = LoggerFactory.getLogger(LinkedInService.class);

    @Value("${linkedin.access-token}")
    private String accessToken;

    @Value("${linkedin.author-urn}")
    private String authorUrn;

    @Value("${linkedin.api-url}")
    private String apiUrl;

    private final WebClient webClient;

    public LinkedInService(@Value("${linkedin.api-url}") String apiUrl,
                           @Value("${linkedin.access-token}") String accessToken) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader("X-Restli-Protocol-Version", "2.0.0")
                .defaultHeader("LinkedIn-Version", "202402")
                .build();
    }

    public String publishPost(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide");
        }

        Map<String, Object> requestBody = buildRequestBody(content);

        try {
            logger.info("Tentative de publication sur LinkedIn: {}",
                    content.substring(0, Math.min(50, content.length())) + "...");

            return webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        String errorMsg = String.format("Erreur LinkedIn API - Status: %s, Body: %s",
                                                response.statusCode(), errorBody);
                                        logger.error(errorMsg);
                                        return Mono.error(new LinkedInApiException(errorMsg));
                                    }))
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            String errorMsg = "Échec de la publication sur LinkedIn: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new LinkedInApiException(errorMsg, e);
        }
    }

    private Map<String, Object> buildRequestBody(String content) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("author", authorUrn);
        requestBody.put("lifecycleState", "PUBLISHED");

        Map<String, Object> shareContent = new LinkedHashMap<>();
        shareContent.put("shareCommentary", Collections.singletonMap("text", content));
        shareContent.put("shareMediaCategory", "NONE");

        Map<String, Object> specificContent = new LinkedHashMap<>();
        specificContent.put("com.linkedin.ugc.ShareContent", shareContent);

        requestBody.put("specificContent", specificContent);
        requestBody.put("visibility", Collections.singletonMap(
                "com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC"
        ));

        return requestBody;
    }

    public static class LinkedInApiException extends RuntimeException {
        public LinkedInApiException(String message) {
            super(message);
        }

        public LinkedInApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

