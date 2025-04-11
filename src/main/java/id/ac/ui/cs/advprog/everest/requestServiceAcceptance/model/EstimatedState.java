package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

public class EstimatedState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.ESTIMATED;
    }

    @Override
    public RequestState processAction(String action, RequestContext context) {
        return null;
    }
}