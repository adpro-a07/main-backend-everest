package id.ac.ui.cs.advprog.everest.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BaseException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN);
        initCause(cause);
    }
}
