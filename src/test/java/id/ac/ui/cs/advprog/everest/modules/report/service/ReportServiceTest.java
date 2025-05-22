package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private UUID reportId;
    private UUID userRequestId;
    private UUID technicianId;
    private UUID customerId;
    private AuthenticatedUser technician;
    private AuthenticatedUser customer;
    private CreateTechnicianReportDraft mockCreateRequest;
    private TechnicianReport mockTechnicianReport;
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

        mockTechnicianReport = TechnicianReport.builder()
                .reportId(reportId)
                .userRequest(mockUserRequest)
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofSeconds(3600L))
                .build();
        mockTechnicianReport.setStatus("COMPLETED");
    }

    private ReportResponse mapToReportResponse(TechnicianReport report) {
        return ReportResponse.builder()
                .id(report.getReportId())
                .technicianId(report.getTechnicianId())
                .diagnosis(report.getDiagnosis())
                .actionPlan(report.getActionPlan())
                .estimatedCost(report.getEstimatedCost())
                .estimatedTimeSeconds(report.getEstimatedTimeSeconds())
                .status(report.getStatus())
                .lastUpdatedAt(report.getLastUpdatedAt())
                .build();
    }

    @Test
    void testGetAllReports() {
        when(reportRepository.findByStatus("COMPLETED")).thenReturn(List.of(mockTechnicianReport));

        List<ReportResponse> result = reportService.getAllReports(technician);

        assertEquals(1, result.size());
        assertEquals(mockTechnicianReport.getTechnicianId(), result.get(0).getTechnicianId());
        verify(reportRepository).findByStatus("COMPLETED");
    }

    @Test
    void testGetReportById() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockTechnicianReport));

        ReportResponse result = reportService.getReportById(reportId, technician);

        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals(mockTechnicianReport.getDiagnosis(), result.getDiagnosis());
        verify(reportRepository).findById(reportId);
    }

    @Test
    void testGetReportByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(reportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                reportService.getReportById(nonExistentId, technician)
        );
        verify(reportRepository).findById(nonExistentId);
    }

    @Test
    void testGetReportsByStatus() {
        when(reportRepository.findByStatus("COMPLETED"))
                .thenReturn(List.of(mockTechnicianReport));

        List<ReportResponse> result = reportService.getReportsByStatus("COMPLETED", technician);

        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
        verify(reportRepository).findByStatus("COMPLETED");
    }

    @Test
    void testGetReportsByStatus_NotCompleted() {
        List<ReportResponse> result = reportService.getReportsByStatus("CANCELLED", technician);
        assertTrue(result.isEmpty());
        verify(reportRepository, never()).findByStatus(any());
    }

    @Test
    void testGetReportsByDiagnosis() {
        when(reportRepository.findByDiagnosisContainingIgnoreCaseAndStatus("screen", "COMPLETED"))
                .thenReturn(List.of(mockTechnicianReport));

        List<ReportResponse> result = reportService.getReportsByDiagnosis("screen", technician);

        assertEquals(1, result.size());
        assertEquals(mockTechnicianReport.getDiagnosis(), result.get(0).getDiagnosis());
        verify(reportRepository).findByDiagnosisContainingIgnoreCaseAndStatus("screen", "COMPLETED");
    }

    @Test
    void testGetReportsByActionPlan() {
        when(reportRepository.findByActionPlanContainingIgnoreCaseAndStatus("replace", "COMPLETED"))
                .thenReturn(List.of(mockTechnicianReport));

        List<ReportResponse> result = reportService.getReportsByActionPlan("replace", technician);

        assertEquals(1, result.size());
        assertEquals(mockTechnicianReport.getActionPlan(), result.get(0).getActionPlan());
        verify(reportRepository).findByActionPlanContainingIgnoreCaseAndStatus("replace", "COMPLETED");
    }

    @Test
    void testGetReportsByTechnicianId() {
        UUID techId = mockTechnicianReport.getTechnicianId();
        when(reportRepository.findByTechnicianIdAndStatus(techId, "COMPLETED"))
                .thenReturn(List.of(mockTechnicianReport));

        List<ReportResponse> result = reportService.getReportsByTechnicianId(techId, technician);

        assertEquals(1, result.size());
        assertEquals(techId, result.get(0).getTechnicianId());
        verify(reportRepository).findByTechnicianIdAndStatus(techId, "COMPLETED");
    }

    @Test
    void testGetAllReports_Empty() {
        when(reportRepository.findByStatus("COMPLETED")).thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getAllReports(technician);
        assertTrue(result.isEmpty(), "Should handle empty repository");
    }

    @Test
    void testGetReportById_NotCompletedStatus() {
        TechnicianReport notCompletedReport = TechnicianReport.builder()
                .reportId(reportId)
                .userRequest(mockUserRequest)
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofSeconds(3600L))
                .build();
        notCompletedReport.setStatus("IN_PROGRESS");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(notCompletedReport));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reportService.getReportById(reportId, technician)
        );
        assertEquals("Report is not completed", exception.getMessage());
        verify(reportRepository).findById(reportId);
    }
}