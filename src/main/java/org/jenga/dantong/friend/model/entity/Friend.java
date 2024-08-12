package org.jenga.dantong.friend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jenga.dantong.global.base.BaseEntity;
import org.jenga.dantong.user.model.entity.User;

@Table(name = "friend")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "friend_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id")
    private User friend;

    @NotNull
    private String userStudentId;
    @NotNull
    private String friendStudentId;
    @Enumerated(EnumType.STRING)
    private FriendStatus status;
    @NotNull
    private boolean isFrom;
    private Long counterpartId;

    public void acceptRequest() {
        status = FriendStatus.ACCEPT;
    }

    public void setCounterpartId(Long id) {
        counterpartId = id;
    }

}
