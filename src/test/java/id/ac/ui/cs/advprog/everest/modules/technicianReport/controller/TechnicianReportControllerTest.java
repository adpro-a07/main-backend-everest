//package id.ac.ui.cs.advprog.everest.modules.technicianReport.controller;
//
//import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
//import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
//import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraft;
//import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
//import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.DatabaseException;
//import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.InvalidTechnicianReportStateException;
//import id.ac.ui.cs.advprog.everest.modules.technicianReport.service.TechnicianReportService;
//import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//public class TechnicianReportControllerTest {
//    private TechnicianReportService technicianReportService;
//    private TechnicianReportController technicianReportController;
//    private AuthenticatedUser user;
//    private AuthenticatedUser technician;
//    private String reportId;
//    private TechnicianReportDraftResponse mockResponse;
//
//    @BeforeEach
//    void setUp() {
//        technicianReportService = mock(TechnicianReportService.class);
//        technicianReportController = new TechnicianReportController(technicianReportService);
//
//        reportId = UUID.randomUUID().toString();
//
//        user = new AuthenticatedUser(
//                UUID.randomUUID(),
//                "customer@example.com",
//                "Customer",
//                UserRole.CUSTOMER,
//                "12301894239",
//                Instant.now(),
//                Instant.now(),
//                "Depok",
//                null,
//                0,
//                0L
//        );
//
//        technician = new AuthenticatedUser(
//                UUID.randomUUID(),
//                "technician@example.com",
//                "Technician",
//                UserRole.TECHNICIAN,
//                "12301894239",
//                Instant.now(),
//                Instant.now(),
//                "Depok",
//                null,
//                0,
//                0L
//        );
//
//        mockResponse = TechnicianReportDraftResponse.builder()
//                .reportId(UUID.fromString(reportId))
//                .userRequestId(UUID.randomUUID())
//                .technicianId(technician.id())
//                .diagnosis("Test diagnosis")
//                .actionPlan("Test action plan")
//                .estimatedCost(new BigDecimal("100.00"))
//                .estimatedTimeSeconds(3600L)
//                .status("DRAFT")
//                .build();
//    }
//
//    @Test
//    void testCreateTechnicianReport() {
//        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
//        request.setDiagnosis("Test diagnosis");
//        request.setActionPlan("Test action plan");
//        request.setEstimatedCost(new BigDecimal("100.00"));
//        request.setEstimatedTimeSeconds(3600L);
//
//        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
//                true,
//                "Technician report created successfully",
//                mockResponse
//        );
//
//        when(technicianReportService.createTechnicianReportDraft(any(), any()))
//                .thenReturn(response);
//
//        ResponseEntity<?> result = technicianReportController.createTechnicianReportDraft(request, technician);
//
//        assertEquals(HttpStatus.CREATED, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).createTechnicianReportDraft(eq(request), eq(technician));
//    }
//
//    @Test
//    void testCreateTechnicianReport_ServiceThrowsException() {
//        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
//        when(technicianReportService.createTechnicianReportDraft(any(), any()))
//                .thenThrow(new InvalidTechnicianReportStateException("Invalid state"));
//
//        try {
//            technicianReportController.createTechnicianReportDraft(request, technician);
//        } catch (InvalidTechnicianReportStateException e) {
//            assertEquals("Invalid state", e.getMessage());
//        }
//
//        verify(technicianReportService).createTechnicianReportDraft(eq(request), eq(technician));
//    }
//
//    @Test
//    void testUpdateTechnicianReport() {
//        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
//        request.setDiagnosis("Updated diagnosis");
//        request.setActionPlan("Updated action plan");
//
//        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
//                true,
//                "Technician report updated successfully",
//                mockResponse
//        );
//
//        when(technicianReportService.updateTechnicianReportDraft(anyString(), any(), any()))
//                .thenReturn(response);
//
//        ResponseEntity<?> result = technicianReportController.updateTechnicianReportDraft(
//                reportId, request, technician);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).updateTechnicianReportDraft(eq(reportId), eq(request), eq(technician));
//    }
//
//    @Test
//    void testDeleteTechnicianReport() {
//        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
//                true,
//                "Technician report deleted successfully",
//                mockResponse
//        );
//
//        when(technicianReportService.deleteTechnicianReportDraft(anyString(), any()))
//                .thenReturn(response);
//
//        ResponseEntity<?> result = technicianReportController.deleteTechnicianReportDraft(reportId, technician);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).deleteTechnicianReportDraft(eq(reportId), eq(technician));
//    }
//
//    @Test
//    void testAcceptTechnicianReport() {
//        GenericResponse<Void> response = new GenericResponse<>(
//                true,
//                "Technician report accepted successfully",
//                null
//        );
//
//        when(technicianReportService.acceptTechnicianReportSubmit(anyString(), any()))
//                .thenReturn(response);
//
//        ResponseEntity<?> result = technicianReportController.acceptTechnicianReportDraft(reportId, user);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).acceptTechnicianReportSubmit(eq(reportId), eq(user));
//    }
//
//    @Test
//    void testRejectTechnicianReport() {
//        GenericResponse<Void> response = new GenericResponse<>(
//                true,
//                "Technician report rejected successfully",
//                null
//        );
//
//        when(technicianReportService.rejectTechnicianReportSubmit(anyString(), any()))
//                .thenReturn(response);
//
//        ResponseEntity<?> result = technicianReportController.rejectTechnicianReportDraft(reportId, user);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).rejectTechnicianReportSubmit(eq(reportId), eq(user));
//    }
//
//    @Test
//    void testAcceptTechnicianReport_ServiceThrowsException() {
//        when(technicianReportService.acceptTechnicianReportSubmit(anyString(), any()))
//                .thenThrow(new DatabaseException("Database error", new RuntimeException()));
//
//        try {
//            technicianReportController.acceptTechnicianReportDraft(reportId, user);
//        } catch (DatabaseException e) {
//            assertEquals("Database error", e.getMessage());
//        }
//
//        verify(technicianReportService).acceptTechnicianReportSubmit(eq(reportId), eq(user));
//    }
//
//    @Test
//    void testRejectTechnicianReport_ServiceThrowsException() {
//        when(technicianReportService.rejectTechnicianReportSubmit(anyString(), any()))
//                .thenThrow(new InvalidTechnicianReportStateException("Cannot reject report"));
//
//        try {
//            technicianReportController.rejectTechnicianReportDraft(reportId, user);
//        } catch (InvalidTechnicianReportStateException e) {
//            assertEquals("Cannot reject report", e.getMessage());
//        }
//
//        verify(technicianReportService).rejectTechnicianReportSubmit(eq(reportId), eq(user));
//    }
//
//    @Test
//    void testGetTechnicianReportsByStatus() {
//        String status = "SUBMITTED";
//        List<TechnicianReportDraftResponse> responseList = List.of(mockResponse);
//
//        GenericResponse<List<TechnicianReportDraftResponse>> serviceResponse = new GenericResponse<>(
//                true,
//                "Technician reports retrieved successfully",
//                responseList
//        );
//
//        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
//                .thenReturn(serviceResponse);
//
//        ResponseEntity<?> result = technicianReportController.getTechnicianReportSubmissions(status, technician);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(technician));
//    }
//
//    @Test
//    void testGetTechnicianReportsByStatus_ServiceThrowsException() {
//        String status = "DRAFT";
//        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
//                .thenThrow(new DatabaseException("Database error", new RuntimeException()));
//
//        try {
//            technicianReportController.getTechnicianReportSubmissions(status, technician);
//        } catch (DatabaseException e) {
//            assertEquals("Database error", e.getMessage());
//        }
//
//        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(technician));
//    }
//
//    @Test
//    void testGetTechnicianReportSubmissions() {
//        String status = "SUBMITTED";
//        List<TechnicianReportDraftResponse> responseList = List.of(mockResponse);
//
//        GenericResponse<List<TechnicianReportDraftResponse>> serviceResponse = new GenericResponse<>(
//                true,
//                "Technician report submissions retrieved successfully",
//                responseList
//        );
//
//        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
//                .thenReturn(serviceResponse);
//
//        ResponseEntity<?> result = technicianReportController.getTechnicianReportSubmissions(status, user);
//
//        assertEquals(HttpStatus.OK, result.getStatusCode());
//        assertNotNull(result.getBody());
//        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(user));
//    }
//
//    @Test
//    void testGetTechnicianReportSubmissions_ServiceThrowsException() {
//        String status = "SUBMITTED";
//        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
//                .thenThrow(new InvalidTechnicianReportStateException("Invalid state"));
//
//        try {
//            technicianReportController.getTechnicianReportSubmissions(status, user);
//        } catch (InvalidTechnicianReportStateException e) {
//            assertEquals("Invalid state", e.getMessage());
//        }
//
//        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(user));
//    }
//}