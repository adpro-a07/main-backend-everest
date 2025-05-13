package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception;

public class InvalidUserRequestStateException extends RuntimeException {
    public InvalidUserRequestStateException(String message) {
        super(message);
    }

    public InvalidUserRequestStateException(String message, Throwable cause) {
        super(message, cause);
    }
}