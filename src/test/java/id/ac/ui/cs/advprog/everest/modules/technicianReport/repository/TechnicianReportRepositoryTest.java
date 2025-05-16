package id.ac.ui.cs.advprog.everest.modules.technicianReport.repository;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.time.Duration;
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

    @Test
    void findAllByTechnicianId_shouldReturnReportsByTechnician() {
        UUID technicianId = UUID.randomUUID();
        UUID otherTechnicianId = UUID.randomUUID();

        UserRequest userRequest1 = new UserRequest(UUID.randomUUID(), "Fix refrigerator");
        UserRequest userRequest2 = new UserRequest(UUID.randomUUID(), "Fix AC");
        entityManager.persist(userRequest1);
        entityManager.persist(userRequest2);

        TechnicianReport report1 = createReport(technicianId, userRequest1, "DRAFT");
        TechnicianReport report2 = createReport(technicianId, userRequest2, "SUBMITTED");
        TechnicianReport report3 = createReport(otherTechnicianId, userRequest1, "DRAFT");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        List<TechnicianReport> reports = technicianReportRepository.findAllByTechnicianId(technicianId);

        assertEquals(2, reports.size());
        assertTrue(reports.contains(report1));
        assertTrue(reports.contains(report2));
        assertFalse(reports.contains(report3));
    }

    @Test
    void findByReportId_shouldReturnCorrectReport() {
        UUID reportId = UUID.randomUUID();
        UserRequest userRequest = new UserRequest(UUID.randomUUID(), "Fix refrigerator");
        entityManager.persist(userRequest);

        TechnicianReport report = createReport(UUID.randomUUID(), userRequest, "DRAFT");
        report.setReportId(reportId);
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
        UserRequest userRequest = new UserRequest(UUID.randomUUID(), "Fix refrigerator");
        entityManager.persist(userRequest);

        TechnicianReport draftReport = createReport(UUID.randomUUID(), userRequest, "DRAFT");
        TechnicianReport submittedReport1 = createReport(UUID.randomUUID(), userRequest, "SUBMITTED");
        TechnicianReport submittedReport2 = createReport(UUID.randomUUID(), userRequest, "SUBMITTED");

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
    void findAllByUserRequestRequestId_shouldReturnReportsForRequest() {
        UUID userId = UUID.randomUUID();
        UUID requestId1 = UUID.randomUUID();
        UUID requestId2 = UUID.randomUUID();

        UserRequest userRequest1 = new UserRequest(userId, "Fix refrigerator");
        UserRequest userRequest2 = new UserRequest(userId, "Fix AC");
        userRequest1.setRequestId(requestId1);
        userRequest2.setRequestId(requestId2);
        entityManager.persist(userRequest1);
        entityManager.persist(userRequest2);

        TechnicianReport report1 = createReport(UUID.randomUUID(), userRequest1, "DRAFT");
        TechnicianReport report2 = createReport(UUID.randomUUID(), userRequest1, "SUBMITTED");
        TechnicianReport report3 = createReport(UUID.randomUUID(), userRequest2, "DRAFT");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Debug to check if relationship is set correctly
        System.out.println("Report1 UserRequest: " +
                (report1.getUserRequest() != null ? report1.getUserRequest().getRequestId() : "null"));
        System.out.println("Report2 UserRequest: " +
                (report2.getUserRequest() != null ? report2.getUserRequest().getRequestId() : "null"));
        System.out.println("RequestId1: " + requestId1);

        List<TechnicianReport> reportsForRequest1 = technicianReportRepository.findAllByUserRequestRequestId(requestId1);

        assertEquals(2, reportsForRequest1.size());
    }

    @Test
    void findAllByTechnicianIdAndStatus_shouldReturnFilteredReports() {
        // Arrange
        UUID technicianId = UUID.randomUUID();

        UserRequest userRequest = new UserRequest(UUID.randomUUID(), "Fix refrigerator");
        entityManager.persist(userRequest);

        TechnicianReport report1 = createReport(technicianId, userRequest, "DRAFT");
        TechnicianReport report2 = createReport(technicianId, userRequest, "SUBMITTED");
        TechnicianReport report3 = createReport(technicianId, userRequest, "DRAFT");
        TechnicianReport report4 = createReport(UUID.randomUUID(), userRequest, "DRAFT");

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

    private TechnicianReport createReport(UUID technicianId, UserRequest userRequest, String status) {
        TechnicianReport report = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .technicianId(technicianId)
                .userRequest(userRequest)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofHours(2))
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