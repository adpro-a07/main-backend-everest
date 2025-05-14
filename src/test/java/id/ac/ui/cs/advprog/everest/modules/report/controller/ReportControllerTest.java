package id.ac.ui.cs.advprog.everest.modules.report.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
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

    private ReportResponse createSampleResponse(String tech, ReportStatus status) {
        return ReportResponse.builder()
                .id(UUID.randomUUID())
                .technicianName(tech)
                .repairDetails("Details")
                .repairDate(LocalDate.now())
                .status(status.name())
                .build();
    }

    @Test
    void testGetAllWithoutFilters() {
        var resp = createSampleResponse("John", ReportStatus.COMPLETED);
        when(reportService.getAllReports(any(AuthenticatedUser.class))).thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, null, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("John", result.getBody().get(0).getTechnicianName());
        verify(reportService).getAllReports(adminUser);
    }

    @Test
    void testGetByTechnician() {
        var resp = createSampleResponse("Alice", ReportStatus.IN_PROGRESS);
        when(reportService.getReportsByTechnician(eq("Alice"), any())).thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getReportList("Alice", null, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Alice", result.getBody().get(0).getTechnicianName());
        verify(reportService).getReportsByTechnician("Alice", adminUser);
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void testGetByStatus() {
        var resp = createSampleResponse("Bob", ReportStatus.CANCELLED);
        when(reportService.getReportsByStatus(eq(ReportStatus.CANCELLED), any())).thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, ReportStatus.CANCELLED, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(ReportStatus.CANCELLED.name(), result.getBody().get(0).getStatus());
        verify(reportService).getReportsByStatus(ReportStatus.CANCELLED, adminUser);
    }

    @Test
    void testGetByTechAndStatus() {
        var resp = createSampleResponse("Charlie", ReportStatus.COMPLETED);
        when(reportService.getReportsByTechnicianAndStatus(eq("Charlie"), eq(ReportStatus.COMPLETED), any()))
                .thenReturn(List.of(resp));

        ResponseEntity<List<ReportResponse>> result = controller.getReportList("Charlie", ReportStatus.COMPLETED, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Charlie", result.getBody().get(0).getTechnicianName());
        assertEquals(ReportStatus.COMPLETED.name(), result.getBody().get(0).getStatus());
        verify(reportService).getReportsByTechnicianAndStatus("Charlie", ReportStatus.COMPLETED, adminUser);
    }

    @Test
    void testGetDetailSuccess() {
        UUID id = UUID.randomUUID();
        var resp = createSampleResponse("Dan", ReportStatus.COMPLETED);
        resp.setId(id);
        when(reportService.getReportById(eq(id), any())).thenReturn(resp);

        ResponseEntity<ReportResponse> result = controller.getReportDetailById(id, adminUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().getId());
        verify(reportService).getReportById(id, adminUser);
    }

    @Test
    void testGetDetailNotFound() {
        UUID id = UUID.randomUUID();
        when(reportService.getReportById(eq(id), any())).thenThrow(new RuntimeException("not found"));

        Exception ex = assertThrows(Exception.class, () -> controller.getReportDetailById(id, adminUser));
        // Controller wraps exception into ResponseStatusException with 404
        assertTrue(ex instanceof org.springframework.web.server.ResponseStatusException);
    }

    @Test
    void testEmptyList() {
        when(reportService.getAllReports(any())).thenReturn(List.of());
        var result = controller.getReportList(null, null, techUser);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void testMultipleItems() {
        var r1 = createSampleResponse("Eve", ReportStatus.IN_PROGRESS);
        var r2 = createSampleResponse("Frank", ReportStatus.COMPLETED);
        when(reportService.getAllReports(any())).thenReturn(List.of(r1, r2));

        var result = controller.getReportList(null, null, adminUser);
        assertEquals(2, result.getBody().size());
        assertEquals("Eve", result.getBody().get(0).getTechnicianName());
        assertEquals("Frank", result.getBody().get(1).getTechnicianName());
    }
}
