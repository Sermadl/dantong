package org.jenga.dantong.survey.model.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SurveyResponse {

    private String title;
    private String description;
    private Long postId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    @Builder.Default
    private List<SurveyItemResponse> surveyItems = new ArrayList<SurveyItemResponse>();
}
