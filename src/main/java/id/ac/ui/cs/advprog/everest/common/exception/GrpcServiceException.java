package id.ac.ui.cs.advprog.everest.common.exception;

public class GrpcServiceException extends RuntimeException {
    public GrpcServiceException(String message) {
        super(message);
    }
}

