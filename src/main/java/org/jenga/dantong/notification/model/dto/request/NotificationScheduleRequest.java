package org.jenga.dantong.notification.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationScheduleRequest {
    private String studentId;
    private String title;
    private String body;
    @Setter
    private LocalDateTime sentAt;
}
