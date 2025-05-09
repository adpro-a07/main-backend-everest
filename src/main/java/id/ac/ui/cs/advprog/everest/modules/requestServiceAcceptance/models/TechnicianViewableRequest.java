package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public interface TechnicianViewableRequest {
    Long getRequestId();
    String getDescription();
    Long getTechnicianId();
    RequestStatus getStatus();
}