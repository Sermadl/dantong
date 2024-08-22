package org.jenga.dantong.survey.model.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyUserAllReplyResponse {

    private Long submitId;
    private Long surveyId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public SurveyUserAllReplyResponse(Long submitId, Long surveyId, String title,
        LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.submitId = submitId;
        this.surveyId = surveyId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
