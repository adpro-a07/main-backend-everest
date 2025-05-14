package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String currentState, String attemptedAction) {
        super("Cannot perform " + attemptedAction + " action while in " + currentState + " state");
    }
}