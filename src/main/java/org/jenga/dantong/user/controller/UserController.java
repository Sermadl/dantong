package org.jenga.dantong.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.user.model.dto.request.DkuInfoRequest;
import org.jenga.dantong.notification.service.FcmService;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.dto.request.LoginRequest;
import org.jenga.dantong.user.model.dto.request.RefreshTokenRequest;
import org.jenga.dantong.user.model.dto.request.SignupRequest;
import org.jenga.dantong.user.model.dto.request.UserInfoEditRequest;
import org.jenga.dantong.user.model.dto.response.LoginResponse;
import org.jenga.dantong.user.model.dto.response.RefreshTokenResponse;
import org.jenga.dantong.user.model.dto.response.UserResponse;
import org.jenga.dantong.user.model.dto.response.VerifiedStudentResponse;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.service.AuthService;
import org.jenga.dantong.user.repository.UserRepository;
import org.jenga.dantong.user.service.UserSignupService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserSignupService userSignupService;
    private final AuthService authService;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    @GetMapping
    @UserAuth
    public UserResponse info(AppAuthentication auth) {
        return userSignupService.userInfo(auth.getUserId());
    }

    @PostMapping(path = "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        LoginResponse loginResponse = userSignupService.login(loginRequest);
//        fcmService.register(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(path = "/logout")
    @UserAuth
    public void logout(AppAuthentication auth) {
        User user = userRepository.findById(auth.getUserId())
                .orElseThrow(UserNotFoundException::new);
        fcmService.deleteToken(user.getStudentId());
    }

    @PostMapping(path = "/reissue")
    @UserAuth
    public RefreshTokenResponse reissue(HttpServletRequest httpServletRequest,
                                        @Valid @RequestBody RefreshTokenRequest dto) {
        return userSignupService.refreshToken(httpServletRequest, dto.getRefreshToken());
    }

    @PostMapping(path = "/signup/{signup-token}")
    public void signup(@RequestBody @Validated SignupRequest request,
                       @PathVariable("signup-token") String signupToken) {
        User user = userSignupService.signup(request, signupToken);
    }

    @PatchMapping(path = "/edit")
    @UserAuth
    public void edit(@RequestBody @Validated UserInfoEditRequest request,
                     AppAuthentication auth) {
        userSignupService.userInfoEdit(request, auth.getUserId());
    }

    @PostMapping("/verify")
    public VerifiedStudentResponse verifyDKUStudent(@Valid @RequestBody DkuInfoRequest request) {
        return authService.verifyDkuInfo(request);
    }
}
