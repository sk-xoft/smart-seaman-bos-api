package com.seaman.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleAuthConfig {

    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/firebase.messaging"
    );

    @Value("${firebase.credentials.path:firebase-service-account.json}")
    private String path;

    @Bean
    GoogleCredentials credentialFromFile() throws IOException {
        ClassPathResource serviceAccount = new ClassPathResource(path);
        return GoogleCredentials.fromStream(serviceAccount.getInputStream())
                .createScoped(SCOPES);
    }

}
