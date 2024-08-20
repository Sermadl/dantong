package org.jenga.dantong.user.model.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class VerifiedStudentResponse {

    private String signupToken;
    private StudentInfo studentInfo;


    public VerifiedStudentResponse(String signupToken, StudentInfo studentInfo) {
        this.signupToken = signupToken;
        this.studentInfo = studentInfo;
    }
}
