package org.jenga.dantong.friend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jenga.dantong.user.model.entity.Major;

@Getter
@Setter
@AllArgsConstructor
public class RequestListResponse {
    String studentId;
    String name;
    Major major;
    Long friendshipId;
}
