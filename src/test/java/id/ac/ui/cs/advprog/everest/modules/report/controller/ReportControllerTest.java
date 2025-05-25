package id.ac.ui.cs.advprog.everest.modules.report.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {
    private ReportService reportService;
    private ReportController controller;
    private AuthenticatedUser adminUser;
    private AuthenticatedUser techUser;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        controller = new ReportController(reportService);
        adminUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "admin@example.com",
                "Admin User",
                UserRole.ADMIN,
                "555-1234",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );
        techUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "tech@example.com",
                "Tech User",
                UserRole.TECHNICIAN,
                "555-5678",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );
    }

    private ReportResponse createSampleResponse(UUID technicianId, String diagnosis, String actionPlan, String status) {
        return ReportResponse.builder()
                .id(UUID.randomUUID())
                .technicianId(technicianId)
                .diagnosis(diagnosis)
                .actionPlan(actionPlan)
                .status(status)
                .build();
    }

    @Test
    void testGetAllCompletedReports() {
        UUID techId = UUID.randomUUID();
        var resp = createSampleResponse(techId, "Diagnosis1", "Action1", "COMPLETED");
        when(reportService.getReportsByStatus(eq("COMPLETED"), any(AuthenticatedUser.class))).thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getAllCompletedReports(adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(techId, result.getBody().get(0).getTechnicianId());
        verify(reportService).getReportsByStatus("COMPLETED", adminUser);
    }

    @Test
    void testGetReportDetailByIdSuccess() {
        UUID id = UUID.randomUUID();
        UUID techId = UUID.randomUUID();
        var resp = createSampleResponse(techId, "Diagnosis3", "Action3", "COMPLETED");
        resp.setId(id);
        when(reportService.getReportById(eq(id), any())).thenReturn(resp);

        ResponseEntity<ReportResponse> result = controller.getReportDetailById(id, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().getId());
        verify(reportService).getReportById(id, adminUser);
    }

    @Test
    void testGetReportDetailByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(reportService.getReportById(eq(id), any())).thenThrow(new RuntimeException("not found"));

        Exception ex = assertThrows(Exception.class, () -> controller.getReportDetailById(id, adminUser));
        assertTrue(ex instanceof org.springframework.web.server.ResponseStatusException);
    }

    @Test
    void testGetAllCompletedReportsEmpty() {
        when(reportService.getReportsByStatus(eq("COMPLETED"), any())).thenReturn(List.of());
        var result = controller.getAllCompletedReports(techUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void testGetCompletedReportsByTechnician() {
        UUID techId = UUID.randomUUID();
        var resp = createSampleResponse(techId, "Diagnosis2", "Action2", "COMPLETED");
        when(reportService.getReportsByTechnicianId(eq(techId), any())).thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getCompletedReportsByTechnicianId(techId, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(techId, result.getBody().get(0).getTechnicianId());
        verify(reportService).getReportsByTechnicianId(techId, adminUser);
    }

}
