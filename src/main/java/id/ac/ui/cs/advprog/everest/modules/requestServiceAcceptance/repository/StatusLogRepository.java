package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.StatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusLogRepository extends JpaRepository<StatusLog, Long> {
    List<StatusLog> findByRequestIdOrderByTimestampDesc(Long requestId);
    List<StatusLog> findByTechnicianIdOrderByTimestampDesc(Long technicianId);
}