package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.IncomingRequest;

import java.util.List;
import java.util.Optional;

public interface TechnicianRequestService {

    IncomingRequest assignRequestToTechnician(Long requestId, Long technicianId);

    Optional<IncomingRequest> getRequestById(Long requestId);

    List<IncomingRequest> getRequestsByTechnician(Long technicianId);

    List<IncomingRequest> getAllRequests();

    boolean deleteRequest(Long requestId);
}
