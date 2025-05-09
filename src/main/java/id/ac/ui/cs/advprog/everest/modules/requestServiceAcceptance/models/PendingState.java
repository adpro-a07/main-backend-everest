package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public class PendingState implements RequestState {
    @Override
    public RequestState processAction(String action, RequestContext context) {
        if ("create_report".equals(action)) {
            TechnicianViewableRequest request = context.getRequest();
            context.logStatusChange(RequestStatus.PENDING, RequestStatus.REPORTED);
            return new ReportedState();
        }
        return this;
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.PENDING;
    }
}