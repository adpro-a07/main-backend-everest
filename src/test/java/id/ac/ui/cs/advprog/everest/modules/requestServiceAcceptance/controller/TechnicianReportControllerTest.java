package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidTechnicianReportStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.TechnicianReportService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TechnicianReportControllerTest {
    private TechnicianReportService technicianReportService;
    private TechnicianReportController technicianReportController;
    private AuthenticatedUser user;
    private AuthenticatedUser technician;
    private String reportId;
    private TechnicianReportDraftResponse mockResponse;

    @BeforeEach
    void setUp() {
        technicianReportService = mock(TechnicianReportService.class);
        technicianReportController = new TechnicianReportController(technicianReportService);

        reportId = UUID.randomUUID().toString();

        user = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@example.com",
                "Customer",
                UserRole.CUSTOMER,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );

        technician = new AuthenticatedUser(
                UUID.randomUUID(),
                "technician@example.com",
                "Technician",
                UserRole.TECHNICIAN,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );

        // Create mock TechnicianReportDraftResponse for testing
        mockResponse = TechnicianReportDraftResponse.builder()
                .reportId(UUID.fromString(reportId))
                .userRequestId(UUID.randomUUID())
                .technicianId(technician.id())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTimeSeconds(3600L)
                .status("DRAFT")
                .build();
    }

    @Test
    void testCreateTechnicianReport() {
        // Arrange
        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
        request.setDiagnosis("Test diagnosis");
        request.setActionPlan("Test action plan");
        request.setEstimatedCost(new BigDecimal("100.00"));
        request.setEstimatedTimeSeconds(3600L);

        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
                true,
                "Technician report created successfully",
                mockResponse
        );

        when(technicianReportService.createTechnicianReportDraft(any(), any()))
                .thenReturn(response);

        // Act
        ResponseEntity<?> result = technicianReportController.createTechnicianReportDraft(request, technician);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).createTechnicianReportDraft(eq(request), eq(technician));
    }

    @Test
    void testCreateTechnicianReport_ServiceThrowsException() {
        // Arrange
        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
        when(technicianReportService.createTechnicianReportDraft(any(), any()))
                .thenThrow(new InvalidTechnicianReportStateException("Invalid state"));

        // Act & Assert
        try {
            technicianReportController.createTechnicianReportDraft(request, technician);
        } catch (InvalidTechnicianReportStateException e) {
            assertEquals("Invalid state", e.getMessage());
        }

        verify(technicianReportService).createTechnicianReportDraft(eq(request), eq(technician));
    }

    @Test
    void testUpdateTechnicianReport() {
        // Arrange
        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
        request.setDiagnosis("Updated diagnosis");
        request.setActionPlan("Updated action plan");

        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
                true,
                "Technician report updated successfully",
                mockResponse
        );

        when(technicianReportService.updateTechnicianReportDraft(anyString(), any(), any()))
                .thenReturn(response);

        // Act
        ResponseEntity<?> result = technicianReportController.updateTechnicianReportDraft(
                reportId, request, technician);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).updateTechnicianReportDraft(eq(reportId), eq(request), eq(technician));
    }

    @Test
    void testDeleteTechnicianReport() {
        // Arrange
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(
                true,
                "Technician report deleted successfully",
                mockResponse
        );

        when(technicianReportService.deleteTechnicianReportDraft(anyString(), any()))
                .thenReturn(response);

        // Act
        ResponseEntity<?> result = technicianReportController.deleteTechnicianReportDraft(reportId, technician);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).deleteTechnicianReportDraft(eq(reportId), eq(technician));
    }

    @Test
    void testAcceptTechnicianReport() {
        // Arrange
        GenericResponse<Void> response = new GenericResponse<>(
                true,
                "Technician report accepted successfully",
                null
        );

        when(technicianReportService.acceptTechnicianReportSubmit(anyString(), any()))
                .thenReturn(response);

        // Act
        ResponseEntity<?> result = technicianReportController.acceptTechnicianReportDraft(reportId, user);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).acceptTechnicianReportSubmit(eq(reportId), eq(user));
    }

    @Test
    void testRejectTechnicianReport() {
        // Arrange
        GenericResponse<Void> response = new GenericResponse<>(
                true,
                "Technician report rejected successfully",
                null
        );

        when(technicianReportService.rejectTechnicianReportSubmit(anyString(), any()))
                .thenReturn(response);

        // Act
        ResponseEntity<?> result = technicianReportController.rejectTechnicianReportDraft(reportId, user);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).rejectTechnicianReportSubmit(eq(reportId), eq(user));
    }

    @Test
    void testAcceptTechnicianReport_ServiceThrowsException() {
        // Arrange
        when(technicianReportService.acceptTechnicianReportSubmit(anyString(), any()))
                .thenThrow(new DatabaseException("Database error", new RuntimeException()));

        // Act & Assert
        try {
            technicianReportController.acceptTechnicianReportDraft(reportId, user);
        } catch (DatabaseException e) {
            assertEquals("Database error", e.getMessage());
        }

        verify(technicianReportService).acceptTechnicianReportSubmit(eq(reportId), eq(user));
    }

    @Test
    void testRejectTechnicianReport_ServiceThrowsException() {
        // Arrange
        when(technicianReportService.rejectTechnicianReportSubmit(anyString(), any()))
                .thenThrow(new InvalidTechnicianReportStateException("Cannot reject report"));

        // Act & Assert
        try {
            technicianReportController.rejectTechnicianReportDraft(reportId, user);
        } catch (InvalidTechnicianReportStateException e) {
            assertEquals("Cannot reject report", e.getMessage());
        }

        verify(technicianReportService).rejectTechnicianReportSubmit(eq(reportId), eq(user));
    }

    @Test
    void testGetTechnicianReportsByStatus() {
        // Arrange
        String status = "DRAFT";
        List<TechnicianReportDraftResponse> responseList = List.of(mockResponse);

        GenericResponse<List<TechnicianReportDraftResponse>> serviceResponse = new GenericResponse<>(
                true,
                "Technician reports retrieved successfully",
                responseList
        );

        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
                .thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> result = technicianReportController.getTechnicianReportSubmissions(status, technician);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(technician));
    }

    @Test
    void testGetTechnicianReportsByStatus_ServiceThrowsException() {
        // Arrange
        String status = "DRAFT";
        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
                .thenThrow(new DatabaseException("Database error", new RuntimeException()));

        // Act & Assert
        try {
            technicianReportController.getTechnicianReportSubmissions(status, technician);
        } catch (DatabaseException e) {
            assertEquals("Database error", e.getMessage());
        }

        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(technician));
    }

    @Test
    void testGetTechnicianReportSubmissions() {
        // Arrange
        String status = "SUBMITTED";
        List<TechnicianReportDraftResponse> responseList = List.of(mockResponse);

        GenericResponse<List<TechnicianReportDraftResponse>> serviceResponse = new GenericResponse<>(
                true,
                "Technician report submissions retrieved successfully",
                responseList
        );

        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
                .thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> result = technicianReportController.getTechnicianReportSubmissions(status, user);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(user));
    }

    @Test
    void testGetTechnicianReportSubmissions_ServiceThrowsException() {
        // Arrange
        String status = "SUBMITTED";
        when(technicianReportService.getTechnicianReportSubmissions(anyString(), any()))
                .thenThrow(new InvalidTechnicianReportStateException("Invalid state"));

        // Act & Assert
        try {
            technicianReportController.getTechnicianReportSubmissions(status, user);
        } catch (InvalidTechnicianReportStateException e) {
            assertEquals("Invalid state", e.getMessage());
        }

        verify(technicianReportService).getTechnicianReportSubmissions(eq(status), eq(user));
    }
}