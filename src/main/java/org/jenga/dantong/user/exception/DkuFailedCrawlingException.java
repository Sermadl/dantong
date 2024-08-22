package org.jenga.dantong.user.exception;

import org.jenga.dantong.global.error.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class DkuFailedCrawlingException extends ApplicationException {
    
    public DkuFailedCrawlingException(Throwable t) {
        super(t, HttpStatus.BAD_REQUEST, "DKU-LOGIN-FAILED");
    }

    public DkuFailedCrawlingException() {
        super(HttpStatus.BAD_REQUEST, "DKU-CRAWLING-FAILED");
    }

    public DkuFailedCrawlingException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        setCustomMessage(message);
    }
}
