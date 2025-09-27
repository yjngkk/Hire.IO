package ma.nexotek.HireCraft.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/oauth2")
@CrossOrigin(origins = "http://localhost:3000")
public class OAuth2CallbackController {

    private static final String REDIRECT_URI = "http://localhost:8089/oauth2/oauth2callback";
    
    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    @GetMapping("/authorize")
    public RedirectView startAuthorization() {
        String url = flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .build();
        return new RedirectView(url);
    }

    @GetMapping("/oauth2callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {
        
        if (error != null) {
            return ResponseEntity.badRequest()
                    .body("Authorization error: " + error);
        }

        if (code == null) {
            return ResponseEntity.badRequest()
                    .body("Code parameter is missing");
        }

        try {
            // Exchange auth code for access token
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();

            // Store the credentials
            flow.createAndStoreCredential(tokenResponse, "user");

            return ResponseEntity.ok("Authorization successful! You can close this window.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error during authorization: " + e.getMessage());
        }
    }
}
