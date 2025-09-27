package ma.nexotek.HireCraft.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.service.TwilioSmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final TwilioSmsService smsService;


    @PostMapping
    public ResponseEntity<Map<String, String>> sendSms(@RequestBody Map<String, String> payload) {
        log.info("Received SMS request: {}", payload);
        
        String to = payload.get("to");
        String message = payload.get("message");
        
        if (to == null || to.isBlank() || message == null || message.isBlank()) {
            log.warn("Invalid SMS request - missing required fields. to: {}, message: {}", to, message);
            return ResponseEntity.badRequest().body(Map.of("error", "'to' and 'message' are required"));
        }
        
        try {
            String sid = smsService.sendSms(to, message);
            log.info("SMS sent successfully with SID: {}", sid);
            return ResponseEntity.ok(Map.of("sid", sid));
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}


