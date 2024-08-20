package org.jenga.dantong.friend.exception;

import org.jenga.dantong.global.error.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class NoFriendsFoundException  extends ApplicationException {

    public NoFriendsFoundException() {
        super(HttpStatus.BAD_REQUEST, "NO_FRIENDS_FOUND");
    }
}
