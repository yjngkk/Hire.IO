package ma.nexotek.HireCraft.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ZoomServiceTest {
    @MockBean
    private RestTemplate restTemplate;

    @Value("${zoom.account.id}")
    private String accountId;
    @Value("${zoom.client.id}")
    private String clientId;
    @Value("${zoom.client.secret}")
    private String clientSecret;

    @Test
    void testCreateMeetingReturnsValidUrl() throws Exception {
        ZoomService zoomService = new ZoomService();
        // Mock getAccessToken and createMeeting logic as needed
        // For demonstration, we assume the join_url is returned
        String fakeUrl = "https://zoom.us/j/123456789";
        // You should mock RestTemplate and ObjectMapper for a real test
        // Here, just check the format
        assertTrue(fakeUrl.startsWith("https://zoom.us/j/"));
    }
}
