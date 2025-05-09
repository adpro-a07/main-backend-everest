package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicianRequestServiceImpl implements TechnicianRequestService {

    private final TechnicianRequestRepository technicianRepository;
    private final UserRequestRepository userRequestRepository;

    public TechnicianRequestServiceImpl(TechnicianRequestRepository technicianRepository,
                                        UserRequestRepository userRequestRepository) {
        this.technicianRepository = technicianRepository;
        this.userRequestRepository = userRequestRepository;
    }

    @Override
    public IncomingRequest assignRequestToTechnician(Long requestId, Long technicianId) {
        if (technicianId == null) {
            throw new IllegalArgumentException("Technician ID cannot be null.");
        }

        if (technicianRepository.findByRequestId(requestId).isPresent()) {
            throw new IllegalStateException("Request is already assigned to a technician.");
        }

        UserRequest userRequest = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("User request not found."));

        IncomingRequest assignedRequest = IncomingRequest.from(userRequest, technicianId);
        return technicianRepository.save(assignedRequest);
    }

    @Override
    public Optional<IncomingRequest> getRequestById(Long requestId) {
        return technicianRepository.findByRequestId(requestId);
    }

    @Override
    public List<IncomingRequest> getRequestsByTechnician(Long technicianId) {
        return technicianRepository.findByTechnicianId(technicianId);
    }

    @Override
    public List<IncomingRequest> getAllRequests() {
        return technicianRepository.findAll();
    }

    @Override
    public boolean deleteRequest(Long requestId) {
        return technicianRepository.deleteByRequestId(requestId);
    }
}
