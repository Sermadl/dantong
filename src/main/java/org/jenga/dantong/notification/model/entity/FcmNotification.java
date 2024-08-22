package org.jenga.dantong.notification.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jenga.dantong.global.base.BaseEntity;
import org.jenga.dantong.user.model.entity.User;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmNotification extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String body;
    private String url;

    public FcmNotification(User user, String title, String body, String url) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.url = url;
    }
}
