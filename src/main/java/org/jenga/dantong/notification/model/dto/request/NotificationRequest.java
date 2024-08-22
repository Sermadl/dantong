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
    private String url;

    public NotificationRequest(String studentId, String title, String body, String url) {
        this.studentId = studentId;
        this.title = title;
        this.body = body;
        this.url = url;
    }
}
