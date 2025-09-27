package ma.nexotek.HireCraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import ma.nexotek.HireCraft.dto.ZoomDto;
import ma.nexotek.HireCraft.mapper.ZoomMapper;
import ma.nexotek.HireCraft.model.ZoomMeeting;
import ma.nexotek.HireCraft.dto.ZoomDto;
import ma.nexotek.HireCraft.mapper.ZoomMapper;

import java.util.List;
import java.time.LocalDateTime;
@Service
public class ZoomService {
    @Value("${zoom.account.id}")
    private String accountId;
    @Value("${zoom.client.id}")
    private String clientId;
    @Value("${zoom.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmailService emailService;

    public String getAccessToken() throws Exception {
        String url = "https://zoom.us/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "account_credentials");
        body.add("account_id", accountId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        JsonNode node = objectMapper.readTree(response.getBody());
        return node.get("access_token").asText();
    }

    public ZoomDto createMeeting(String topic, String candidateEmail, LocalDateTime dateTime) throws Exception {
        String accessToken = getAccessToken();
        String url = "https://api.zoom.us/v2/users/me/meetings";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Ensure Zoom returns JSON
        // Add start_time in ISO format
        String startTime = dateTime != null ? "\"start_time\": \"" + dateTime.toString() + "\"," : "";
        String body = "{" + startTime + "\"topic\": \"" + topic + "\", \"type\": 1}";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
            throw new RuntimeException("Zoom API error: " + response.getStatusCode() + "\nBody: " + response.getBody());
        }
        JsonNode node = objectMapper.readTree(response.getBody());
        if (node.has("join_url")) {
            ZoomMeeting meeting = ZoomMeeting.builder()
                .zoomId(node.has("id") ? node.get("id").asText() : null)
                .topic(topic)
                .joinUrl(node.get("join_url").asText())
                .meetingDate(dateTime)
                .build();
            ZoomDto zoomDto = ZoomMapper.toDto(meeting);
            sendZoomLinkToCandidate(candidateEmail, topic, dateTime, zoomDto.getJoinUrl());
            return zoomDto;
        } else {
            throw new RuntimeException("Zoom API did not return join_url. Response: " + response.getBody());
        }
    }

    private void sendZoomLinkToCandidate(String candidateEmail, String topic, LocalDateTime dateTime, String joinUrl) {
        try {
            // Use topic as position and candidateEmail as name for now
            emailService.sendZoomMeetingEmail(
                candidateEmail, // candidateName
                candidateEmail, // candidateEmail
                topic, // position
                dateTime,
                joinUrl
            );
        } catch (Exception e) {
            System.err.println("Failed to send email to candidate: " + e.getMessage());
        }
    }

}
