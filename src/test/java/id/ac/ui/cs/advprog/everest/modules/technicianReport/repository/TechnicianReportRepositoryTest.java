package id.ac.ui.cs.advprog.everest.modules.technicianReport.repository;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TechnicianReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TechnicianReportRepository technicianReportRepository;

    private UUID technicianId;
    private UUID otherTechnicianId;
    private RepairOrder repairOrder1;
    private RepairOrder repairOrder2;
    private RepairOrder repairOrder3;

    private TechnicianReport report1;
    private TechnicianReport report2;
    private TechnicianReport report3;

    @BeforeEach
    void setUp() {
        this.technicianId = UUID.randomUUID();
        this.otherTechnicianId = UUID.randomUUID();

        this.repairOrder1 = RepairOrder.builder()
                .customerId(UUID.randomUUID())
                .technicianId(technicianId)
                .itemName("Refrigerator")
                .itemCondition("Broken")
                .issueDescription("Not cooling")
                .desiredServiceDate(LocalDate.now())
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        this.repairOrder2 = RepairOrder.builder()
                .customerId(UUID.randomUUID())
                .technicianId(otherTechnicianId)
                .itemName("AC")
                .itemCondition("Broken")
                .issueDescription("Not cooling")
                .desiredServiceDate(LocalDate.now())
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        this.repairOrder3 = RepairOrder.builder()
                .customerId(UUID.randomUUID())
                .technicianId(technicianId)
                .itemName("Washing Machine")
                .itemCondition("Broken")
                .issueDescription("Not spinning")
                .desiredServiceDate(LocalDate.now())
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        this.report1 = createReport(technicianId, repairOrder1, "DRAFT");
        this.report2 = createReport(otherTechnicianId, repairOrder2, "DRAFT");
        this.report3 = createReport(technicianId, repairOrder3, "DRAFT");
    }

    @Test
    void findAllByTechnicianId_shouldReturnReportsByTechnician() {
        entityManager.persist(repairOrder1);
        entityManager.persist(repairOrder2);
        entityManager.persist(repairOrder3);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        List<TechnicianReport> reports = technicianReportRepository.findAllByTechnicianId(technicianId);

        assertEquals(2, reports.size());
        assertTrue(reports.contains(report1));
        assertFalse(reports.contains(report2));
        assertTrue(reports.contains(report3));
    }

    @Test
    void findByReportId_shouldReturnCorrectReport() {
        entityManager.persist(repairOrder1);
        TechnicianReport report = report1;
        UUID reportId = report.getReportId();

        report1.setReportId(reportId);
        entityManager.persist(report);
        entityManager.flush();

        Optional<TechnicianReport> foundReport = technicianReportRepository.findByReportId(reportId);
        Optional<TechnicianReport> notFoundReport = technicianReportRepository.findByReportId(UUID.randomUUID());

        assertTrue(foundReport.isPresent());
        assertEquals(reportId, foundReport.get().getReportId());
        assertFalse(notFoundReport.isPresent());
    }

    @Test
    void findAllByStatus_shouldReturnReportsWithMatchingStatus() {
        entityManager.persist(repairOrder1);

        TechnicianReport draftReport = createReport(technicianId, repairOrder1, "DRAFT");
        TechnicianReport submittedReport1 = createReport(technicianId, repairOrder1, "SUBMITTED");
        TechnicianReport submittedReport2 = createReport(technicianId, repairOrder1, "SUBMITTED");

        entityManager.persist(draftReport);
        entityManager.persist(submittedReport1);
        entityManager.persist(submittedReport2);
        entityManager.flush();

        List<TechnicianReport> draftReports = technicianReportRepository.findAllByStatus("DRAFT");
        List<TechnicianReport> submittedReports = technicianReportRepository.findAllByStatus("SUBMITTED");
        List<TechnicianReport> approvedReports = technicianReportRepository.findAllByStatus("APPROVED");

        assertEquals(1, draftReports.size());
        assertEquals(2, submittedReports.size());
        assertEquals(0, approvedReports.size());
    }

    @Test
    void findAllByTechnicianIdAndStatus_shouldReturnFilteredReports() {
        UUID technicianId = UUID.randomUUID();

        entityManager.persist(repairOrder1);

        TechnicianReport report1 = createReport(technicianId, repairOrder1, "DRAFT");
        TechnicianReport report2 = createReport(technicianId, repairOrder1, "SUBMITTED");
        TechnicianReport report3 = createReport(technicianId, repairOrder1, "DRAFT");
        TechnicianReport report4 = createReport(UUID.randomUUID(), repairOrder1, "DRAFT");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.persist(report4);
        entityManager.flush();

        List<TechnicianReport> technicianDraftReports =
                technicianReportRepository.findAllByTechnicianIdAndStatus(technicianId, "DRAFT");

        assertEquals(2, technicianDraftReports.size());
        assertTrue(technicianDraftReports.stream().allMatch(
                report -> report.getTechnicianId().equals(technicianId) && report.getStatus().equals("DRAFT")
        ));
    }

    private TechnicianReport createReport(UUID technicianId, RepairOrder repairOrder, String status) {
        TechnicianReport report = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .technicianId(technicianId)
                .repairOrder(repairOrder)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTimeSeconds(3600L)
                .build();

        switch(status) {
            case "SUBMITTED":
                report.submit();
                break;
            case "APPROVED":
                report.submit();
                report.approve();
                break;
            case "REJECTED":
                report.submit();
                report.reject();
                break;
            case "IN_PROGRESS":
                report.submit();
                report.approve();
                report.startWork();
                break;
            case "COMPLETED":
                report.submit();
                report.approve();
                report.startWork();
                report.complete();
                break;
        }
        return report;
    }
}