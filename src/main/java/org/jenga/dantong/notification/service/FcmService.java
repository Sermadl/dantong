package org.jenga.dantong.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.repository.FcmRepository;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService implements NotificationService{

    private final FcmRepository fcmRepository;
    private final Map<Long, String> tokenMap = new HashMap<>();

    public void sendSubmitNotification(String studentId) {
        if (!hasKey(studentId)) {
            log.info("error");
            throw new UserNotFoundException();
        }
        String token = getToken(studentId);
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("설문 등록 완료!")
                        .setBody("신청이 완료 되었습니다.")
                        .build())
                .putData("title", "편지 도착 알림")
                .putData("content", "편지가 도착했습니다.")
                .build();

        send(message);
    }


    private void send(Message message) {
        FirebaseMessaging.getInstance().sendAsync(message);
        log.info("Send Notification Success");
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
        log.info("Delete Token Success");
    }

}