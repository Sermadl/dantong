package org.jenga.dantong.notification.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenRegisterRequest {
    private String studentId;
    private String token;
}
