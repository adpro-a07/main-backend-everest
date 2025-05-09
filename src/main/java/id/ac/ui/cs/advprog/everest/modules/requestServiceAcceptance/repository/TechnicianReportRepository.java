package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.TechnicianReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianReportRepository extends JpaRepository<TechnicianReport, Long> {
    Optional<TechnicianReport> findByRequestId(Long requestId);
    List<TechnicianReport> findByTechnicianId(Long technicianId);
}