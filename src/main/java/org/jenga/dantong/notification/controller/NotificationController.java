package org.jenga.dantong.notification.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.notification.model.dto.request.NotificationGlobalRequest;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.model.dto.request.TokenRegisterRequest;
import org.jenga.dantong.notification.service.FcmService;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/notification")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    @UserAuth
    @PostMapping("/register")
    @Operation(summary = "FCM 토큰 등록하기", description = "리액트에서 발급받은 토큰 입력")
    public void register(@RequestBody TokenRegisterRequest request, AppAuthentication auth) {
        fcmService.register(request);
    }

    @UserAuth
    @PostMapping("/send")
    @Operation(summary = "백그라운드 알림 테스트")
    public void send(@RequestBody NotificationRequest request, AppAuthentication auth) {
        User user = userRepository.findById(auth.getUserId())
                .orElseThrow(UserNotFoundException::new);
        fcmService.sendNotification(request);
    }

    @UserAuth
    @PostMapping("/sendGlobal")
    @Operation(summary = "전체 알림 테스트")
    public void sendGlobal(@RequestBody NotificationGlobalRequest request, AppAuthentication auth) throws FirebaseMessagingException {
        userRepository.findById(auth.getUserId())
                .orElseThrow(UserNotFoundException::new);
        fcmService.sendGlobalNotification(request);
    }

}
