package id.ac.ui.cs.advprog.everest.modules.technicianReport.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.service.TechnicianReportService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TechnicianReportControllerTest {

    private TechnicianReportService technicianReportService;
    private TechnicianReportController controller;
    private AuthenticatedUser technician;
    private AuthenticatedUser customer;
    private TechnicianReportDraftResponse draftResponse;

    @BeforeEach
    void setUp() {
        technicianReportService = mock(TechnicianReportService.class);
        controller = new TechnicianReportController(technicianReportService);

        technician = new AuthenticatedUser(
                UUID.randomUUID(), "tech@example.com", "Tech", UserRole.TECHNICIAN,
                "1234567890", Instant.now(), Instant.now(), "Jakarta", null, 0, 0L
        );
        customer = new AuthenticatedUser(
                UUID.randomUUID(), "cust@example.com", "Cust", UserRole.CUSTOMER,
                "0987654321", Instant.now(), Instant.now(), "Jakarta", null, 0, 0L
        );
        draftResponse = TechnicianReportDraftResponse.builder()
                .reportId(UUID.randomUUID())
                .repairOrderId(UUID.randomUUID())
                .technicianId(technician.id())
                .diagnosis("Diagnosis")
                .actionPlan("Action")
                .estimatedCost(10L)
                .estimatedTimeSeconds(1000L)
                .build();
    }

    @Test
    void createTechnicianReportDraft_HappyPath() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder().build();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Created", draftResponse);

        when(technicianReportService.createTechnicianReportDraft(request, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.createTechnicianReportDraft(request, technician);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTechnicianReportDraft_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder().build();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Updated", draftResponse);

        when(technicianReportService.updateTechnicianReportDraft(reportId, request, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.updateTechnicianReportDraft(reportId, request, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteTechnicianReportDraft_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Deleted", draftResponse);

        when(technicianReportService.deleteTechnicianReportDraft(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.deleteTechnicianReportDraft(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void submitTechnicianReportDraft_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Submitted", draftResponse);

        when(technicianReportService.submitTechnicianReportDraft(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.submitTechnicianReportDraft(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void acceptTechnicianReportSubmit_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<Void> response = new GenericResponse<>(true, "Accepted", null);

        when(technicianReportService.acceptTechnicianReportSubmit(reportId, customer)).thenReturn(response);

        ResponseEntity<?> result = controller.acceptTechnicianReportSubmit(reportId, customer);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void rejectTechnicianReportSubmit_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<Void> response = new GenericResponse<>(true, "Rejected", null);

        when(technicianReportService.rejectTechnicianReportSubmit(reportId, customer)).thenReturn(response);

        ResponseEntity<?> result = controller.rejectTechnicianReportSubmit(reportId, customer);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void startWork_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Started", draftResponse);

        when(technicianReportService.startWork(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.startWork(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void completeWork_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "Completed", draftResponse);

        when(technicianReportService.completeWork(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.completeWork(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTechnicianReportByStatusForTechnician_HappyPath() {
        List<TechnicianReportDraftResponse> list = Collections.singletonList(draftResponse);
        GenericResponse<List<TechnicianReportDraftResponse>> response = new GenericResponse<>(true, "OK", list);

        when(technicianReportService.getTechnicianReportByStatusForTechnician("DRAFT", technician)).thenReturn(response);

        ResponseEntity<?> result = controller.getTechnicianReportByStatusForTechnician("DRAFT", technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTechnicianReportByStatusForCustomer_HappyPath() {
        List<TechnicianReportDraftResponse> list = Collections.singletonList(draftResponse);
        GenericResponse<List<TechnicianReportDraftResponse>> response = new GenericResponse<>(true, "OK", list);

        when(technicianReportService.getTechnicianReportByStatusForCustomer("SUBMITTED", customer)).thenReturn(response);

        ResponseEntity<?> result = controller.getTechnicianReportByStatusForCustomer("SUBMITTED", customer);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void createTechnicianReportDraft_Failed() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder().build();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(false, "Failed", null);

        when(technicianReportService.createTechnicianReportDraft(request, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.createTechnicianReportDraft(request, technician);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTechnicianReportDraft_Failed() {
        String reportId = draftResponse.getReportId().toString();
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder().build();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(false, "Update failed", null);

        when(technicianReportService.updateTechnicianReportDraft(reportId, request, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.updateTechnicianReportDraft(reportId, request, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteTechnicianReportDraft_Failed() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(false, "Delete failed", null);

        when(technicianReportService.deleteTechnicianReportDraft(reportId, technician)).thenReturn(response);
    }

    @Test
    void getTechnicianReportById_HappyPath() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(true, "OK", draftResponse);

        when(technicianReportService.getTechnicianReportById(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.getTechnicianReportById(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTechnicianReportById_Failed_NotFound() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(false, "Not found", null);

        when(technicianReportService.getTechnicianReportById(reportId, technician)).thenReturn(response);

        ResponseEntity<?> result = controller.getTechnicianReportById(reportId, technician);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTechnicianReportById_Failed_Unauthorized() {
        String reportId = draftResponse.getReportId().toString();
        GenericResponse<TechnicianReportDraftResponse> response = new GenericResponse<>(false, "Unauthorized", null);

        when(technicianReportService.getTechnicianReportById(reportId, customer)).thenReturn(response);

        ResponseEntity<?> result = controller.getTechnicianReportById(reportId, customer);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}