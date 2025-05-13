package id.ac.ui.cs.advprog.everest.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
