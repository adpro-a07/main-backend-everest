package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

public interface TechnicianViewableRequest {
    Long getRequestId();
    String getDescription();
    Long getTechnicianId();
    RequestStatus getStatus();
}