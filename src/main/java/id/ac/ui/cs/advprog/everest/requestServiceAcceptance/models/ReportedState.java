package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

public class ReportedState implements RequestState {
    @Override
    public RequestState processAction(String action, RequestContext context) {
        if ("create_estimate".equals(action)) {
            context.logStatusChange(RequestStatus.REPORTED, RequestStatus.ESTIMATED);
            return new EstimatedState();
        }
        return this;
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.REPORTED;
    }
}