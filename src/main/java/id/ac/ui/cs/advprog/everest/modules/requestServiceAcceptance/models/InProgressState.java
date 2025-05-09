package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public class InProgressState implements RequestState {
    @Override
    public RequestState processAction(String action, RequestContext context) {
        // Add transitions for completing work or other actions as needed
        return this;
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.IN_PROGRESS;
    }
}