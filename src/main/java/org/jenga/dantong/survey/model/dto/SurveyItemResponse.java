package org.jenga.dantong.survey.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SurveyItemResponse {

    private Long surveyItemId;
    private String title;
    private String description;
}
