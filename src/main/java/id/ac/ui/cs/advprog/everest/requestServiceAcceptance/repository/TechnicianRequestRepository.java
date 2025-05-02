package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.IncomingRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TechnicianRequestRepository {
    private final Map<Long, IncomingRequest> technicianRequests = new ConcurrentHashMap<>();

    public IncomingRequest save(IncomingRequest incomingRequest) {
        if  (incomingRequest.getTechnicianId() == null){
            incomingRequest = null;
            throw new IllegalArgumentException("Technician Id is null");
        }

        IncomingRequest existing = technicianRequests.get(incomingRequest.getRequestId());
        if (existing != null) {
            throw new IllegalArgumentException("Request Id: " + incomingRequest.getRequestId() + "Already taken by technician " + existing.getTechnicianId());
        }

        technicianRequests.put(incomingRequest.getRequestId(), incomingRequest);
        return incomingRequest;
    }

    public Optional<IncomingRequest> findByRequestId(Long requestId) {
        return Optional.ofNullable(technicianRequests.get(requestId));
    }

    public List<IncomingRequest> findByTechnicianId(Long technicianId) {
        return technicianRequests.values().stream()
                .filter(request -> request.getTechnicianId().equals(technicianId))
                .collect(Collectors.toList());
    }

    public List<IncomingRequest> findAll() {
        return new ArrayList<>(technicianRequests.values());
    }

    public boolean deleteByRequestId(Long requestId) {
        return technicianRequests.remove(requestId) != null;
    }
}