package org.jenga.dantong.notification.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationGlobalRequest {
    private String title;
    private String body;
    private String url;
}
