package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

public interface RequestState {
    RequestState processAction(String action, RequestContext context);
    RequestStatus getStatus();
}