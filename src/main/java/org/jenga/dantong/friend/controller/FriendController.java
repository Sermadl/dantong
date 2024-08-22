package org.jenga.dantong.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.friend.model.dto.response.FriendListResponse;
import org.jenga.dantong.friend.model.dto.response.RequestListResponse;
import org.jenga.dantong.friend.model.dto.response.SubmitFriendListResponse;
import org.jenga.dantong.friend.service.FriendService;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.notification.service.FcmService;
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
    private final FcmService fcmService;

    @UserAuth
    @PostMapping("/send/{studentId}")
    @Operation(summary = "친구 요청 보내기", description = "학번으로 친구 요청 보내기")
    public void sendRequest(@Valid @PathVariable("studentId") String studentId, AppAuthentication auth) {
        friendService.sendRequest(studentId, auth.getUserId());
        fcmService.sendFriendNotification(studentId);
    }

    @UserAuth
    @GetMapping("/request")
    @Operation(summary = "받은 친구 요청 리스트 조회", description = "권한 확인 후 요청 리스트 조회")
    public ResponseEntity<Page<RequestListResponse>> getRequestList(AppAuthentication auth, Pageable pageable) {
        return ResponseEntity.ok(friendService.getRequestList(auth.getUserId(), pageable));
    }

    @UserAuth
    @PostMapping("/accept/{friendshipId}")
    @Operation(summary = "친구 요청 수락", description = "친구 요청 Id로 친구 요청 수락")
    public void acceptRequest(@PathVariable("friendshipId") Long friendId, AppAuthentication auth) {
        friendService.acceptRequest(friendId, auth.getUserId());
    }

    @UserAuth
    @DeleteMapping("/deleteRequest/{friendshipId}")
    @Operation(summary = "친구 요청 삭제", description = "친구 요청 Id로 친구 요청 삭제")
    public void deleteRequest(@PathVariable("friendshipId") Long friendId, AppAuthentication auth) {
        friendService.deleteRequest(friendId, auth.getUserId());
    }

    @UserAuth
    @DeleteMapping("/deleteFriend/{friendshipId}")
    @Operation(summary = "친구 삭제", description = "친구 Id로 친구 삭제")
    public void deleteFriend(@PathVariable("friendshipId") Long friendshipId, AppAuthentication auth) {
        friendService.deleteFriend(friendshipId, auth.getUserId());
    }

    @UserAuth
    @GetMapping("/list")
    @Operation(summary = "친구 리스트 조회", description = "권한 확인 후 친구 리스트 조회")
    public ResponseEntity<Page<FriendListResponse>> getFriendList(AppAuthentication auth, Pageable pageable) {
        return ResponseEntity.ok(friendService.getFriendList(auth.getUserId(), pageable));
    }

    @UserAuth
    @GetMapping("/submit-list/studentId/{friendStudentId}")
    @Operation(summary = "친구 학번으로 친구가 신청한 행사 리스트 확인하기", description = "친구 등록 후 친구 학번으로 조회 가능")
    public ResponseEntity<List<TicketResponse>> viewFriendSubmit(@PathVariable("friendStudentId") String studentId, AppAuthentication auth) {
        return ResponseEntity.ok(friendService.viewSubmitByStudentId(studentId, auth.getUserId()));
    }

    @UserAuth
    @GetMapping("/submit-list/postId/{postId}")
    @Operation(summary = "게시글에서 신청한 친구 리스트 확인하기", description = "친구 등록 후 postId로 조회 가능")
    public ResponseEntity<List<SubmitFriendListResponse>> viewSubmitAtPost(@PathVariable("postId") Long postId, AppAuthentication auth) {
        return ResponseEntity.ok(friendService.viewSubmitByPost(postId, auth.getUserId()));
    }
}
