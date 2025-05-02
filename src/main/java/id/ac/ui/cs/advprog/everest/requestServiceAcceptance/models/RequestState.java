package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

public interface RequestState {
    RequestState processAction(String action, RequestContext context);
    RequestStatus getStatus();
}