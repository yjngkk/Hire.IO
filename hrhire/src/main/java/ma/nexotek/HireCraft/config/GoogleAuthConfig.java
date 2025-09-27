package ma.nexotek.HireCraft.config;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

@Configuration
public class GoogleAuthConfig {

    private static final String CREDENTIALS_FILE_PATH = "client_secret_698217671023.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(),
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH))
        );

        // Create tokens directory if it doesn't exist
        File tokensDirectory = new File(TOKENS_DIRECTORY_PATH);
        if (!tokensDirectory.exists()) {
            tokensDirectory.mkdirs();
        }

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                Arrays.asList("https://www.googleapis.com/auth/calendar")
        )
        .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory))
        .setAccessType("offline")
        .build();
    }
}
