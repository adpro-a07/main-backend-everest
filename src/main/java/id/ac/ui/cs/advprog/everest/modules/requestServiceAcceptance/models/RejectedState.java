package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public class RejectedState implements RequestState {
    @Override
    public RequestState processAction(String action, RequestContext context) {
        // Terminal state - no transitions out
        return this;
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.REJECTED;
    }
}