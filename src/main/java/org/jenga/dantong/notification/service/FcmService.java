package org.jenga.dantong.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.repository.FcmRepository;
import org.jenga.dantong.user.model.dto.request.LoginRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService implements NotificationService{

    private final FcmRepository fcmRepository;
    private final Map<Long, String> tokenMap = new HashMap<>();

    public void sendEventNotification(String studentId) {
        if (!hasKey(studentId)) {
            log.info("error");
            return;
        }
        String token = getToken(studentId);
        log.info(studentId);
        log.info(token);
        Message message1 = Message.builder()
                .setWebpushConfig(
                        WebpushConfig.builder()
                                .putHeader("ttl", "300")
                                .setNotification(new WebpushNotification("신청 완료 알림",
                                        "신청이 완료되었습니다."))
                                .build()
                )
                .setToken(token)
                .build();
        send(message1);
        Message message2 = Message.builder()
                .putData("title", "편지 도착 알림")
                .putData("content", "편지가 도착했습니다.")
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("설문 등록 완료!")
                        .setBody("신청이 완료 되었습니다.")
                        .build())
                .build();
        send(message2);
    }


    private void send(Message message) {
        FirebaseMessaging.getInstance().sendAsync(message);
    }

    private String getToken(String studentId) {
        return fcmRepository.getToken(studentId);
    }

    private boolean hasKey(String studentId) {
        return fcmRepository.hasKey(studentId);
    }

    public void register(NotificationRequest request){
        fcmRepository.saveToken(request);
    }

    public void deleteToken(String studentId){
        fcmRepository.deleteToken(studentId);
    }

}