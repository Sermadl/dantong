package org.jenga.dantong.notification.model.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class NotificationRequest {
    private String studentId;
    private String title;
    private String body;

    public NotificationRequest(String studentId, String title, String body) {
        this.studentId = studentId;
        this.title = title;
        this.body = body;
    }
}
