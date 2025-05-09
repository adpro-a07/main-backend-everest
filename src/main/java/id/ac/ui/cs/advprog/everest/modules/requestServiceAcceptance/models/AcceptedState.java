package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public class AcceptedState implements RequestState {
    @Override
    public RequestState processAction(String action, RequestContext context) {
        if ("start_work".equals(action)) {
            context.logStatusChange(RequestStatus.ACCEPTED, RequestStatus.IN_PROGRESS);
            return new InProgressState();
        }
        return this;
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.ACCEPTED;
    }
}


