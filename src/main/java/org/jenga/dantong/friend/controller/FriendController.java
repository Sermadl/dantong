package org.jenga.dantong.friend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.friend.model.dto.response.FriendListResponse;
import org.jenga.dantong.friend.model.dto.response.RequestListResponse;
import org.jenga.dantong.friend.service.FriendService;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.survey.model.dto.response.TicketResponse;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/friend")
@RestController
@RequiredArgsConstructor
public class FriendController {
    private final UserRepository userRepository;
    private final FriendService friendService;

    @UserAuth
    @PostMapping("/send/{studentId}")
    public void sendRequest(@Valid @PathVariable("studentId") String studentId, AppAuthentication auth) {
        friendService.sendRequest(studentId, auth.getUserId());
    }

    @UserAuth
    @GetMapping("/request")
    public ResponseEntity<Page<RequestListResponse>> getRequestList(AppAuthentication auth, Pageable pageable) {
        return ResponseEntity.ok(friendService.getRequestList(auth.getUserId(), pageable));
    }

    @UserAuth
    @PostMapping("/accept/{friendId}")
    public void acceptRequest(@PathVariable("friendId") Long friendId, AppAuthentication auth) {
        friendService.acceptRequest(friendId, auth.getUserId());
    }

    @UserAuth
    @GetMapping("/list")
    public ResponseEntity<Page<FriendListResponse>> getFriendList(AppAuthentication auth, Pageable pageable) {
        return ResponseEntity.ok(friendService.getFriendList(auth.getUserId(), pageable));
    }

    @UserAuth
    @GetMapping("/submit-list/studentId/{friendStudentId}")
    public ResponseEntity<List<TicketResponse>> viewFriendSubmit(@PathVariable("friendStudentId") String studentId, AppAuthentication auth) {
        return ResponseEntity.ok(friendService.viewSubmitByStudentId(studentId, auth.getUserId()));
    }

    @UserAuth
    @GetMapping("/submit-list/postId/{postId}")
    public ResponseEntity<List<FriendListResponse>> viewSubmitAtPost(@PathVariable("postId") Long postId, AppAuthentication auth) {
        return ResponseEntity.ok(friendService.viewSubmitByPost(postId, auth.getUserId()));
    }
}
