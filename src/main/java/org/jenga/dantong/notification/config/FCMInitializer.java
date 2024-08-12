package org.jenga.dantong.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
@Slf4j
public class FCMInitializer {


    @Value("${fcm.certification}")
    private String FIREBASE_CONFIG_PATH;


    @PostConstruct
    public void initFcm() {

        try {
            FileInputStream fileInputStream = new FileInputStream(FIREBASE_CONFIG_PATH);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(fileInputStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp initialization complete");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
