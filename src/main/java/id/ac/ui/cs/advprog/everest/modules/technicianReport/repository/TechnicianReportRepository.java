package id.ac.ui.cs.advprog.everest.modules.technicianReport.repository;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicianReportRepository extends JpaRepository<TechnicianReport, UUID> {
    List<TechnicianReport> findAllByTechnicianId(UUID technicianId);

    Optional<TechnicianReport> findByReportId(UUID reportId);

    List<TechnicianReport> findAllByStatus(String status);

    List<TechnicianReport> findAllByRepairOrderId(UUID id);

    List<TechnicianReport> findAllByTechnicianIdAndStatus(UUID technicianId, String status);

    // TODO: Add method to find all technician reports completed state
}