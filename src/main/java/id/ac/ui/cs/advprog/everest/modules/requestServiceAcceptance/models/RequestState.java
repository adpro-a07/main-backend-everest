package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public interface RequestState {
    RequestState processAction(String action, RequestContext context);
    RequestStatus getStatus();
}