package org.jenga.dantong.user.model.dto.response;

public class VerifiedStudentResponse {

    private final String signupToken;
    private final StudentInfo studentInfo;


    public VerifiedStudentResponse(String signupToken, StudentInfo studentInfo) {
        this.signupToken = signupToken;
        this.studentInfo = studentInfo;
    }
}
