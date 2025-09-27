package ma.nexotek.HireCraft.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Register custom LocalDateTime deserializer
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        mapper.registerModule(module);

        return mapper;
    }

    /**
     * Custom deserializer that handles multiple LocalDateTime formats
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final DateTimeFormatter[] SUPPORTED_FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),    // 2025-09-06T20:15:00.000
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),       // 2025-09-06T20:15:00
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),          // 2025-09-06T20:15
                DateTimeFormatter.ISO_LOCAL_DATE_TIME                       // Standard ISO format
        };

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            String dateTimeString = parser.getValueAsString();

            if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
                return null;
            }

            dateTimeString = dateTimeString.trim();

            // Try each formatter in order
            for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateTimeString, formatter);
                } catch (DateTimeParseException e) {
                    // Continue to next formatter
                }
            }

            // If none of the formatters worked, throw a descriptive error
            throw new IOException(String.format(
                    "Unable to parse LocalDateTime from '%s'. Supported formats: yyyy-MM-dd'T'HH:mm, yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss.SSS",
                    dateTimeString
            ));
        }
    }
}
