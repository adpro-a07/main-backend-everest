package id.ac.ui.cs.advprog.everest.common.exception;

import org.springframework.http.HttpStatus;

public class GrpcServiceException extends BaseException {
    public GrpcServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public GrpcServiceException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}
