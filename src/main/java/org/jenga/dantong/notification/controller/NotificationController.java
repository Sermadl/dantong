package org.jenga.dantong.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.service.FcmService;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/register")
    public void register(@RequestBody NotificationRequest request, AppAuthentication auth) {
        fcmService.register(request);
    }

}
