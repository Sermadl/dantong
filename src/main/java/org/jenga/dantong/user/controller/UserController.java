package org.jenga.dantong.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jenga.dantong.user.model.dto.LoginRequest;
import org.jenga.dantong.user.model.dto.LoginResponse;
import org.jenga.dantong.user.model.dto.SignupRequest;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.service.UserSignupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserSignupService userSignupService;

    @PostMapping(path = "/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userSignupService.login(loginRequest);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(path = "/signup/{signup-token}")
    public void signup(@Valid @RequestBody SignupRequest request,
        @PathVariable("signup-token") String signupToken) {
        User user = userSignupService.signup(request, signupToken);
    }
}
