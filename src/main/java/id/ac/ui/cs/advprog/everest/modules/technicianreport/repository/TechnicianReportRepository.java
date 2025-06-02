package id.ac.ui.cs.advprog.everest.modules.technicianreport.repository;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicianReportRepository extends JpaRepository<TechnicianReport, UUID> {
    Optional<TechnicianReport> findByReportId(UUID reportId);

    List<TechnicianReport> findAllByStatus(String status);

    List<TechnicianReport> findAllByRepairOrderId(UUID id);

    List<TechnicianReport> findAllByTechnicianIdAndStatus(UUID technicianId, String status);

    boolean existsByRepairOrderIdAndStatusNot(UUID repairOrderId, String status);
}