package org.jenga.dantong.notification.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationRequest {
    private String studentId;
    private String token;
}
