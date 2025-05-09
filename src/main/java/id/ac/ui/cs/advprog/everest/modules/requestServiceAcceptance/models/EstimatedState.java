package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public class EstimatedState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.ESTIMATED;
    }

    @Override
    public RequestState processAction(String action, RequestContext context) {
        switch (action) {
            case "accept":
                context.logStatusChange(RequestStatus.ESTIMATED, RequestStatus.ACCEPTED);
                return new AcceptedState();
            case "reject":
                context.logStatusChange(RequestStatus.ESTIMATED, RequestStatus.REJECTED);
                return new RejectedState();
            default:
                return this;
        }
    }
}