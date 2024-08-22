package org.jenga.dantong.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.survey.model.dto.request.SurveyReplyUpdateRequest;
import org.jenga.dantong.survey.model.dto.response.AllRepliesResponse;
import org.jenga.dantong.survey.model.dto.response.SurveyUserAllReplyResponse;
import org.jenga.dantong.survey.model.dto.response.SurveyUserReplyResponse;
import org.jenga.dantong.survey.service.SurveyReplyService;
import org.jenga.dantong.survey.service.SurveySubmitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/reply")
@RequiredArgsConstructor
@RestController
public class SurveyReplyController {

    private final SurveyReplyService surveyReplyService;
    private final SurveySubmitService surveySubmitService;

    @GetMapping("/{surveyItemId}")
    @Operation(summary = "질문 별 응답 리스트 조회", description = "surveyItemId로 질문 특정 후 모든 응답 조회")
    public ResponseEntity<List<SurveyUserReplyResponse>> findReply(
            @PathVariable("surveyItemId") Long surveyItemId) {
        List<SurveyUserReplyResponse> reply = surveyReplyService.findAllReplyBySurveyItem(
                surveyItemId);

        return ResponseEntity.ok(reply);
    }

    /**
     * 질문 항목별 응답 리스트 나열
     *
     * @param surveyId 설문 번호
     * @return
     */
    @GetMapping("/all/{surveyId}")
    @Operation(summary = "모든 응답 조회", description = "surveyId로 설문 특정 후 질문 별 모든 응답 조회")
    public ResponseEntity<List<AllRepliesResponse>> findAllReply(
            @PathVariable("surveyId") Long surveyId) {
        List<AllRepliesResponse> replies = surveyReplyService.findAllReplyBySurvey(
                surveyId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/user/{surveyId}")
    @Operation(summary = "로그인 된 사용자의 특정 설문 응답 조회", description = "token과 설문번호로 권한 확인 후 사용자가 작성한 응답 조회")
    @UserAuth
    public ResponseEntity<List<SurveyUserReplyResponse>> findUserReply(
            @PathVariable("surveyId") Long surveyId, AppAuthentication auth) {
        List<SurveyUserReplyResponse> reply = surveyReplyService.findUserReply(surveyId,
                auth.getUserId());

        return ResponseEntity.ok(reply);
    }

    @GetMapping("/user")
    @Operation(summary = "로그인 된 사용자가 응답한 모든 설문 조회", description = "token과 설문번호로 권한 확인 후 사용자가 작성한 모든 응답 조회")
    @UserAuth
    public ResponseEntity<Page<SurveyUserAllReplyResponse>> findAllUserReply(AppAuthentication auth,
                                                                             Pageable pageable) {
        Page<SurveyUserAllReplyResponse> response = surveyReplyService.findAllUserReply(auth.getUserId(), pageable);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{surveyId}")
    @Operation(summary = "설문 응답 수정", description = "token으로 권한 확인 후 설문 응답 수정")
    @UserAuth
    public void updateReply(@PathVariable(name = "surveyId") Long surveyId,
                            @RequestBody @Validated List<SurveyReplyUpdateRequest> reply,
                            AppAuthentication auth) {

        surveyReplyService.updateReply(surveyId, reply, auth.getUserId());
    }

    @DeleteMapping("/{surveyId}")
    @Operation(summary = "설문 응답 삭제", description = "token과 설문번호로 권한 확인 후 설문 응답 삭제")
    @UserAuth
    public void deleteUserReply(@PathVariable(name = "surveyId") Long surveyId,
                                AppAuthentication auth) {

        surveyReplyService.deleteUserReply(surveyId, auth.getUserId());
    }
}
