package com.seaman.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleAuthConfig {

    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/firebase.messaging"
    );

    @Value("${fcm.firebase.credential.file}")
    private String authFirebaseFileName;

    @Bean
    GoogleCredentials credentialFromFile() throws IOException {
        Resource serviceAccount = authFirebaseFileName.startsWith("/")
                ? new FileSystemResource(authFirebaseFileName)
                : new ClassPathResource(authFirebaseFileName);
        return GoogleCredentials.fromStream(serviceAccount.getInputStream())
                .createScoped(SCOPES);
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}
