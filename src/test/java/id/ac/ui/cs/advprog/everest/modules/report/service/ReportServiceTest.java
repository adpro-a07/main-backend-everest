package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private UUID technicianId;
    private UUID customerId;
    private UUID repairOrderId;
    private AuthenticatedUser technician;
    private TechnicianReport mockTechnicianReport;
    private RepairOrder mockRepairOrder;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        repairOrderId = UUID.randomUUID();
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


        mockRepairOrder = RepairOrder.builder()
                .id(repairOrderId)
                .customerId(customerId)
                .technicianId(technicianId)
                .itemName("Test item")
                .itemCondition("Test condition")
                .issueDescription("Test issue")
                .createdAt(LocalDateTime.now())
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .build();

        mockTechnicianReport = TechnicianReport.builder()
                .reportId(reportId)
                .repairOrder(mockRepairOrder)
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .status("COMPLETED")
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
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
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