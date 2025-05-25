package id.ac.ui.cs.advprog.everest.modules.report.repository;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReportRepository extends JpaRepository<TechnicianReport, UUID> {

    // Find all COMPLETED reports
    List<TechnicianReport> findByStatus(String status);

    List<TechnicianReport> findByTechnicianIdAndStatus(UUID technicianId, String status);

    // Find COMPLETED reports by diagnosis
    List<TechnicianReport> findByDiagnosisContainingIgnoreCaseAndStatus(String diagnosis, String status);

    // Find COMPLETED reports by action plan
    List<TechnicianReport> findByActionPlanContainingIgnoreCaseAndStatus(String actionPlan, String status);

    // COMPLETED reports by technicianId
    @Query("SELECT r FROM TechnicianReport r WHERE r.technicianId = :technicianId AND r.status = 'COMPLETED'")
    List<TechnicianReport> searchByTechnicianIdCompleted(@Param("technicianId") UUID technicianId);
}
