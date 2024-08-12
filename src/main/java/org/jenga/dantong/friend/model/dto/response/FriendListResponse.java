package org.jenga.dantong.friend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jenga.dantong.user.model.entity.Major;

@Getter
@Setter
@AllArgsConstructor
public class FriendListResponse {
    String studentId;
    Major major;
    String name;

    //Todo
    // - 사용자 아이콘(?) 넣기
}
