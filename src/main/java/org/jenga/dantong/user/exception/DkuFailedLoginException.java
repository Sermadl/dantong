package org.jenga.dantong.user.exception;

import org.jenga.dantong.global.error.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class DkuFailedLoginException extends ApplicationException {

    public DkuFailedLoginException(Throwable t) {
        super(t, HttpStatus.BAD_REQUEST, "DKU-LOGIN-FAILED");
    }

    public DkuFailedLoginException() {
        super(HttpStatus.BAD_REQUEST, "DKU-LOGIN-FAILED");
    }

    public DkuFailedLoginException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        setCustomMessage(message);
    }
}
