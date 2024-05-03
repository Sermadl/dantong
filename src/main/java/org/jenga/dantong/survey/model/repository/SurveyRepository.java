package org.jenga.dantong.survey.model.repository;

import org.jenga.dantong.survey.model.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Survey findBySurveyId(int surveyId);
//    Survey findBySurveyIdAndSurveyItem_ShownTrue(int surveyId);
}
