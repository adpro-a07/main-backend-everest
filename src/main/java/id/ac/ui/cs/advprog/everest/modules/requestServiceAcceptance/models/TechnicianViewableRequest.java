package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

public interface TechnicianViewableRequest {
    String getRequestId();
    String getDescription();
    String getTechnicianId();
    RequestStatus getStatus();
}