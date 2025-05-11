package id.ac.ui.cs.advprog.everest.modules.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.excecption.ReportExceptionHandler;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ReportController.class)
@Import({
        GlobalExceptionHandler.class,
        ReportExceptionHandler.class
})
class ReportControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ReportService reportService;

    @MockBean
    private AuthServiceGrpcClient authServiceGrpcClient;

    @Autowired
    private ObjectMapper objectMapper;

    private ReportController controller;
    private AuthenticatedUser adminUser;
    private AuthenticatedUser techUser;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        controller = new ReportController(reportService);

        // Membuat objek AuthenticatedUser untuk testing
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

    private ReportResponse createSampleReportResponse(String technician, ReportStatus status) {
        return ReportResponse.builder()
                .id(UUID.randomUUID())
                .technicianName(technician)
                .repairDetails("Test repair details")
                .repairDate(LocalDate.now())
                .status(status.name())
                .build();
    }

    @Test
    void testGetAllReportsWithoutFilters() {
        ReportResponse response = createSampleReportResponse("John", ReportStatus.COMPLETED);
        List<ReportResponse> responseList = List.of(response);
        when(reportService.getAllReports(any(AuthenticatedUser.class))).thenReturn(responseList);

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, null, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("John", result.getBody().get(0).getTechnicianName());
        verify(reportService).getAllReports(adminUser);
    }

    @Test
    void testGetReportsByTechnician() {
        String technicianName = "John";
        ReportResponse response = createSampleReportResponse(technicianName, ReportStatus.COMPLETED);
        List<ReportResponse> responseList = List.of(response);
        when(reportService.getReportsByTechnician(eq(technicianName), any(AuthenticatedUser.class)))
                .thenReturn(responseList);

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(technicianName, null, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(technicianName, result.getBody().get(0).getTechnicianName());
        verify(reportService).getReportsByTechnician(technicianName, adminUser);
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void testGetReportsByStatus() {
        ReportStatus status = ReportStatus.IN_PROGRESS;
        ReportResponse response = createSampleReportResponse("John", status);
        List<ReportResponse> responseList = List.of(response);
        when(reportService.getReportsByStatus(eq(status), any(AuthenticatedUser.class)))
                .thenReturn(responseList);

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, status, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(status.name(), result.getBody().get(0).getStatus());
        verify(reportService).getReportsByStatus(status, adminUser);
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void testGetReportsByTechnicianAndStatus() {
        String technicianName = "John";
        ReportStatus status = ReportStatus.COMPLETED;
        ReportResponse response = createSampleReportResponse(technicianName, status);
        List<ReportResponse> responseList = List.of(response);
        when(reportService.getReportsByTechnicianAndStatus(
                eq(technicianName), eq(status), any(AuthenticatedUser.class)))
                .thenReturn(responseList);

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(technicianName, status, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(technicianName, result.getBody().get(0).getTechnicianName());
        assertEquals(status.name(), result.getBody().get(0).getStatus());
        verify(reportService).getReportsByTechnicianAndStatus(technicianName, status, adminUser);
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void testGetReportDetailById_Success() {
        UUID reportId = UUID.randomUUID();
        ReportResponse response = createSampleReportResponse("John", ReportStatus.COMPLETED);
        response.setId(reportId);
        when(reportService.getReportById(eq(reportId), any(AuthenticatedUser.class)))
                .thenReturn(response);

        ResponseEntity<ReportResponse> result = controller.getReportDetailById(reportId, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(reportId, result.getBody().getId());
        assertEquals("John", result.getBody().getTechnicianName());

        // Verifikasi bahwa getReportById dipanggil dengan parameter yang benar
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(reportService).getReportById(idCaptor.capture(), eq(adminUser));
        assertEquals(reportId, idCaptor.getValue());
    }

    @Test
    void testGetReportDetailById_WithTechnicianUser() {
        UUID reportId = UUID.randomUUID();
        ReportResponse response = createSampleReportResponse("John", ReportStatus.COMPLETED);
        response.setId(reportId);
        when(reportService.getReportById(eq(reportId), any(AuthenticatedUser.class)))
                .thenReturn(response);

        ResponseEntity<ReportResponse> result = controller.getReportDetailById(reportId, techUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(reportId, result.getBody().getId());
        verify(reportService).getReportById(reportId, techUser);
    }

    @Test
    void testGetReportDetailById_NotFound() {
        UUID reportId = UUID.randomUUID();
        when(reportService.getReportById(eq(reportId), any(AuthenticatedUser.class)))
                .thenThrow(new RuntimeException("Report not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.getReportDetailById(reportId, adminUser));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Report not found with id: " + reportId));
        verify(reportService).getReportById(reportId, adminUser);
    }

    @Test
    void testGetReportList_EmptyList() {
        when(reportService.getAllReports(any(AuthenticatedUser.class))).thenReturn(List.of());

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, null, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(reportService).getAllReports(adminUser);
    }

    @Test
    void testGetReportList_MultipleItems() {
        ReportResponse report1 = createSampleReportResponse("John", ReportStatus.COMPLETED);
        ReportResponse report2 = createSampleReportResponse("Jane", ReportStatus.IN_PROGRESS);
        List<ReportResponse> responseList = List.of(report1, report2);

        when(reportService.getAllReports(any(AuthenticatedUser.class))).thenReturn(responseList);

        ResponseEntity<List<ReportResponse>> result = controller.getReportList(null, null, adminUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertEquals("John", result.getBody().get(0).getTechnicianName());
        assertEquals("Jane", result.getBody().get(1).getTechnicianName());
        verify(reportService).getAllReports(adminUser);
    }
}
