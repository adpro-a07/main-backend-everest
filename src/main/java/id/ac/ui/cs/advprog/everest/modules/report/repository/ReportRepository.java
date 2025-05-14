package id.ac.ui.cs.advprog.everest.modules.report.repository;

import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByTechnicianNameContainingIgnoreCase(String technicianName);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByTechnicianNameContainingIgnoreCaseAndStatus(
            String technicianName,
            ReportStatus status
    );

    @Query("SELECT r FROM Report r WHERE LOWER(r.technicianName) LIKE LOWER(CONCAT('%',:technicianName,'%')) " +
            "AND r.status = :status")
    List<Report> searchByTechnicianAndStatus(
            @Param("technicianName") String technicianName,
            @Param("status") ReportStatus status
    );

    @Query("SELECT r FROM Report r WHERE r.repairDate = :date")
    List<Report> findByRepairDate(@Param("date") LocalDate date);

    @Query("SELECT r FROM Report r WHERE r.repairDate BETWEEN :startDate AND :endDate")
    List<Report> findByRepairDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
