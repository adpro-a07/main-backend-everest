package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

public interface TechnicianViewableRequest {
    Long getRequestId();
    String getDescription();
    Long getTechnicianId();
    RequestStatus getStatus();
}