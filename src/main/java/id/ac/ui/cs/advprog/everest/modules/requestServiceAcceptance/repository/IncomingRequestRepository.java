package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomingRequestRepository extends JpaRepository<IncomingRequest, Long> {
    List<IncomingRequest> findByTechnicianId(Long technicianId);
    List<IncomingRequest> findByTechnicianIdAndStatus(Long technicianId, RequestStatus status);
    Optional<IncomingRequest> findByRequestIdAndTechnicianId(Long requestId, Long technicianId);
}