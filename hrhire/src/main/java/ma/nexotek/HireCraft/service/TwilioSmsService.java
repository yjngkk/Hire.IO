package ma.nexotek.HireCraft.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone}")
    private String fromPhoneNumber;

    private volatile boolean initialized = false;

    @PostConstruct
    public void initializeTwilio() {
        try {
            if (accountSid != null && authToken != null) {
                Twilio.init(accountSid, authToken);
                initialized = true;
                log.info("Twilio initialized successfully");
            } else {
                log.warn("Twilio credentials are not configured. SMS won't be sent.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Twilio", e);
        }
    }

    public String sendSms(String toPhoneNumber, String body) {
        log.info("Attempting to send SMS to: {} with body: {}", toPhoneNumber, body);
        
        if (!initialized) {
            log.warn("Twilio not initialized, attempting to initialize...");
            initializeTwilio();
        }

        if (fromPhoneNumber == null || fromPhoneNumber.isBlank()) {
            log.error("Twilio from phone number is not configured. Current value: {}", fromPhoneNumber);
            throw new IllegalStateException("Twilio from phone number is not configured");
        }

        if (accountSid == null || accountSid.isBlank()) {
            log.error("Twilio account SID is not configured. Current value: {}", accountSid);
            throw new IllegalStateException("Twilio account SID is not configured");
        }

        if (authToken == null || authToken.isBlank()) {
            log.error("Twilio auth token is not configured");
            throw new IllegalStateException("Twilio auth token is not configured");
        }

        try {
            log.info("Sending SMS from {} to {}", fromPhoneNumber, toPhoneNumber);
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    body
            ).create();

            log.info("SMS sent successfully to {} with SID {}", toPhoneNumber, message.getSid());
            return message.getSid();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
}


