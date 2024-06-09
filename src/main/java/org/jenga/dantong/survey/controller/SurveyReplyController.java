package org.jenga.dantong.survey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.auth.jwt.AppAuthentication;
import org.jenga.dantong.global.base.UserAuth;
import org.jenga.dantong.survey.model.dto.request.SurveyReplyUpdateRequest;
import org.jenga.dantong.survey.model.dto.response.SurveyUserReplyResponse;
import org.jenga.dantong.survey.service.SurveyReplyService;
import org.jenga.dantong.survey.service.SurveySubmitService;
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
    public ResponseEntity<List<SurveyUserReplyResponse>> findAllReply(
            @PathVariable("surveyItemId") Long surveyItemId) {
        List<SurveyUserReplyResponse> reply = surveyReplyService.findAllReply(surveyItemId);

        return ResponseEntity.ok(reply);
    }

    @GetMapping("/user/{surveyId}")
    @UserAuth
    public ResponseEntity<List<SurveyUserReplyResponse>> findUserReply(
            @PathVariable("surveyId") Long surveyId, AppAuthentication auth) {
        List<SurveyUserReplyResponse> reply = surveyReplyService.findUserReply(surveyId,
                auth.getUserId());

        return ResponseEntity.ok(reply);
    }


    @PatchMapping("/{surveyId}")
    @UserAuth
    public void updateReply(@PathVariable(name = "surveyId") Long surveyId,
                            @RequestBody @Validated List<SurveyReplyUpdateRequest> reply,
                            AppAuthentication auth) {

        surveyReplyService.updateReply(surveyId, reply, auth.getUserId());
    }

    @DeleteMapping("/{surveyId}")
    @UserAuth
    public void deleteUserReply(@PathVariable(name = "surveyId") Long surveyId,
                                AppAuthentication auth) {

        surveyReplyService.deleteUserReply(surveyId, auth.getUserId());
    }
}
