package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface TechnicianReportRepository extends JpaRepository<TechnicianReport, UUID> {
    List<TechnicianReport> findByTechnicianId(UUID technicianId);
    Optional<TechnicianReport> findByUserRequestAndTechnicianId(UserRequest userRequest, UUID technicianId);
    List<TechnicianReport> findByUserRequest_RequestId(UUID requestId);
}