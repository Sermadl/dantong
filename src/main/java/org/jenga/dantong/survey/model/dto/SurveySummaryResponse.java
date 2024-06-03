package org.jenga.dantong.survey.model.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.jenga.dantong.survey.model.entity.Survey;

@Getter
@Setter
public class SurveySummaryResponse {

    private String title;
    private Long postId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public SurveySummaryResponse(Survey survey) {
        this.title = survey.getTitle();
        this.postId = survey.getPost().getPostId();
        this.startTime = getStartTime();
        this.endTime = getEndTime();
    }
}
