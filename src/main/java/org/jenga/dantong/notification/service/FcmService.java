package org.jenga.dantong.notification.service;

import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.notification.model.dto.request.NotificationGlobalRequest;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.model.dto.request.TokenRegisterRequest;
import org.jenga.dantong.notification.model.entity.FcmNotification;
import org.jenga.dantong.notification.repository.FcmNotificationRepository;
import org.jenga.dantong.notification.repository.FcmRepository;
import org.jenga.dantong.post.exception.PostNofFoundException;
import org.jenga.dantong.post.model.entity.Post;
import org.jenga.dantong.post.repository.PostRepository;
import org.jenga.dantong.survey.exception.SurveyNotFoundException;
import org.jenga.dantong.survey.model.entity.Survey;
import org.jenga.dantong.survey.repository.SurveyRepository;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService implements NotificationService{

    private final FcmRepository fcmRepository;
    private final FcmNotificationRepository fcmNotificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SurveyRepository surveyRepository;
    private final Map<Long, String> tokenMap = new HashMap<>();
    private static final ZoneOffset KST_OFFSET = ZoneOffset.ofHours(9);
    private final TaskScheduler taskScheduler;

    public void sendNotification(NotificationRequest notificationRequest) {
        if (!hasKey(notificationRequest.getStudentId())) {
            log.info("error");
            throw new UserNotFoundException();
        }
        String token = getToken(notificationRequest.getStudentId());
        String title = notificationRequest.getTitle();
        String body = notificationRequest.getBody();

        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                // 안드로이드
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build())
                // 아이폰
                .setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-priority", "10")
                        .setAps(Aps.builder()
                                .setAlert(ApsAlert.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .setBadge(42)
                                .build())
                        .build())
                .build();

        send(message);

        FcmNotification fcmNotification = new FcmNotification(
                userRepository.findByStudentId(notificationRequest.getStudentId())
                        .orElseThrow(UserNotFoundException::new),
                title, body
        );
        fcmNotificationRepository.save(fcmNotification);
    }

    public void sendSubmitNotification(String studentId) {
        NotificationRequest notificationRequest = new NotificationRequest(
                studentId,
                "설문 제출 완료",
                "설문이 성공적으로 제출되었습니다!"
        );

        sendNotification(notificationRequest);
    }

    public void sendFriendNotification(String studentId) {
        NotificationRequest notificationRequest = new NotificationRequest(
                studentId,
                "친구 요청 도착",
                "친구 요청이 도착했습니다!"
        );

        sendNotification(notificationRequest);
    }

    @Transactional
    public void sendEventReminder(NotificationRequest request, Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(SurveyNotFoundException::new);
        Post post = postRepository.findById(survey.getPost().getPostId())
                .orElseThrow(PostNofFoundException::new);

        LocalDateTime sendAt = post.getStartDate().minusMinutes(10);

        taskScheduler.schedule(
                () -> sendNotification(request), sendAt.toInstant(KST_OFFSET)
        );
    }

    public void sendGlobalNotification(NotificationGlobalRequest request) throws FirebaseMessagingException {
        List<String> tokens = fcmRepository.getAllTokens();

        MulticastMessage message = MulticastMessage.builder()
                .putData("title", request.getTitle())
                .putData("body", request.getBody())
                .addAllTokens(tokens)
                .build();

        FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }

    public void send(Message message) {
        FirebaseMessaging.getInstance().sendAsync(message);
        log.info("Send Notification Success");
    }

    private String getToken(String studentId) {
        return fcmRepository.getToken(studentId);
    }

    private boolean hasKey(String studentId) {
        return fcmRepository.hasKey(studentId);
    }

    public void register(TokenRegisterRequest request){
        fcmRepository.saveToken(request);
    }

    public void deleteToken(String studentId){
        fcmRepository.deleteToken(studentId);
        log.info("Delete Token Success");
    }

}