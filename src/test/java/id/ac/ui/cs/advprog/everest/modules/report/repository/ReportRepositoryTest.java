package id.ac.ui.cs.advprog.everest.modules.report.repository;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.UserRequestRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRequestRepository userRequestRepository;

    private TechnicianReport sampleReport;

    private UUID reportId;
    private UUID userRequestId;
    private UUID technicianId;
    private UUID customerId;
    private AuthenticatedUser technician;
    private AuthenticatedUser customer;
    private CreateTechnicianReportDraft mockCreateRequest;
    private UserRequest mockUserRequest;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        userRequestId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        technician = new AuthenticatedUser(
                technicianId,
                "technician@example.com",
                "Test Technician",
                UserRole.TECHNICIAN,
                "1234567890",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );

        customer = new AuthenticatedUser(
                customerId,
                "customer@example.com",
                "Test Customer",
                UserRole.CUSTOMER,
                "0987654321",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );

        mockCreateRequest = new CreateTechnicianReportDraft();
        mockCreateRequest.setUserRequestId(userRequestId.toString());
        mockCreateRequest.setDiagnosis("Test diagnosis");
        mockCreateRequest.setActionPlan("Test action plan");
        mockCreateRequest.setEstimatedCost(new BigDecimal("100.00"));
        mockCreateRequest.setEstimatedTimeSeconds(3600L);

        mockUserRequest = new UserRequest();
        mockUserRequest.setRequestId(userRequestId);
        mockUserRequest.setUserId(customerId);
        mockUserRequest.setUserDescription("Test user request");

        sampleReport = TechnicianReport.builder()
                .reportId(reportId)
                .userRequest(mockUserRequest)
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofSeconds(3600L))
                .build();
        sampleReport.setStatus("COMPLETED");
        userRequestRepository.save(mockUserRequest);
    }

    @Test
    void testSaveNewReport() {
        TechnicianReport saved = reportRepository.save(sampleReport);

        assertThat(saved).isNotNull();
        assertThat(saved.getReportId()).isNotNull();
        assertThat(saved.getDiagnosis()).isEqualTo(sampleReport.getDiagnosis());

        List<TechnicianReport> all = reportRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void testFindById() {
        TechnicianReport saved = reportRepository.save(sampleReport);
        UUID id = saved.getReportId();

        Optional<TechnicianReport> found = reportRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getReportId()).isEqualTo(id);
        assertThat(found.get().getDiagnosis()).isEqualTo(sampleReport.getDiagnosis());
    }

    @Test
    void testFindByIdWithNonExistentId() {
        Optional<TechnicianReport> found = reportRepository.findById(UUID.randomUUID());
        assertThat(found).isNotPresent();
    }

    @Test
    void testUpdateExistingReport() {
        TechnicianReport saved = reportRepository.save(sampleReport);
        saved.setDiagnosis("Updated diagnosis");
        saved.setStatus("COMPLETED");

        TechnicianReport updated = reportRepository.save(saved);
        assertThat(updated.getDiagnosis()).isEqualTo("Updated diagnosis");
        assertThat(updated.getStatus()).isEqualTo("COMPLETED");

        List<TechnicianReport> all = reportRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void testFindByStatus() {
        reportRepository.save(sampleReport);
        TechnicianReport pending = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .userRequest(mockUserRequest)
                .technicianId(UUID.randomUUID())
                .diagnosis("Other")
                .actionPlan("Test")
                .estimatedCost(new BigDecimal("50.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();
        pending.setStatus("IN_PROGRESS");
        pending.setLastUpdatedAt(LocalDateTime.now());
        reportRepository.save(pending);

        List<TechnicianReport> completed = reportRepository.findByStatus("COMPLETED");
        assertThat(completed).hasSize(1);

        List<TechnicianReport> inProgress = reportRepository.findByStatus("IN_PROGRESS");
        assertThat(inProgress).hasSize(1);

        List<TechnicianReport> canceled = reportRepository.findByStatus("CANCELLED");
        assertThat(canceled).isEmpty();
    }

    @Test
    void testFindByDiagnosisContainingIgnoreCaseAndStatus() {
        // Set sampleReport's diagnosis to include "screen"
        sampleReport.setDiagnosis("Screen issue");
        reportRepository.save(sampleReport);
        TechnicianReport other = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .userRequest(mockUserRequest)
                .technicianId(UUID.randomUUID())
                .diagnosis("Battery issue")
                .actionPlan("Replace battery")
                .estimatedCost(new BigDecimal("80.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();
        other.setStatus("COMPLETED");
        other.setLastUpdatedAt(LocalDateTime.now());
        reportRepository.save(other);

        List<TechnicianReport> found = reportRepository.findByDiagnosisContainingIgnoreCaseAndStatus("screen", "COMPLETED");
        assertThat(found).hasSize(1);

        List<TechnicianReport> found2 = reportRepository.findByDiagnosisContainingIgnoreCaseAndStatus("battery", "COMPLETED");
        assertThat(found2).hasSize(1);

        List<TechnicianReport> none = reportRepository.findByDiagnosisContainingIgnoreCaseAndStatus("motherboard", "COMPLETED");
        assertThat(none).isEmpty();
    }

    @Test
    void testFindByActionPlanContainingIgnoreCaseAndStatus() {
        // Set sampleReport's action plan to include "replace"
        sampleReport.setActionPlan("Replace screen");
        reportRepository.save(sampleReport);
        TechnicianReport other = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .userRequest(mockUserRequest)
                .technicianId(UUID.randomUUID())
                .diagnosis("Other")
                .actionPlan("Replace battery")
                .estimatedCost(new BigDecimal("80.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();
        other.setStatus("COMPLETED");
        other.setLastUpdatedAt(LocalDateTime.now());
        reportRepository.save(other);

        List<TechnicianReport> found = reportRepository.findByActionPlanContainingIgnoreCaseAndStatus("replace", "COMPLETED");
        assertThat(found).hasSize(2);

        List<TechnicianReport> none = reportRepository.findByActionPlanContainingIgnoreCaseAndStatus("install", "COMPLETED");
        assertThat(none).isEmpty();
    }

    @Test
    void testSearchByTechnicianIdCompleted() {
        UUID techId = UUID.randomUUID();
        TechnicianReport report1 = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .userRequest(mockUserRequest)
                .technicianId(techId)
                .diagnosis("A")
                .actionPlan("B")
                .estimatedCost(new BigDecimal("10.00"))
                .estimatedTime(Duration.ofMinutes(30))
                .build();
        report1.setStatus("COMPLETED");
        report1.setLastUpdatedAt(LocalDateTime.now());
        reportRepository.save(report1);

        TechnicianReport report2 = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .userRequest(mockUserRequest)
                .technicianId(techId)
                .diagnosis("C")
                .actionPlan("D")
                .estimatedCost(new BigDecimal("20.00"))
                .estimatedTime(Duration.ofMinutes(60))
                .build();
        report2.setStatus("IN_PROGRESS");
        report2.setLastUpdatedAt(LocalDateTime.now());
        reportRepository.save(report2);

        List<TechnicianReport> completed = reportRepository.searchByTechnicianIdCompleted(techId);
        assertThat(completed).hasSize(1);
        assertThat(completed.get(0).getStatus()).isEqualTo("COMPLETED");
    }
}
