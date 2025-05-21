package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.InvalidTechnicianReportStateException;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.UserRequestRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianReportServiceImplTest {

    @Mock
    private TechnicianReportRepository technicianReportRepository;

    @Mock
    private UserRequestRepository userRequestRepository;

    @InjectMocks
    private TechnicianReportServiceImpl technicianReportService;

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
    }

    @Test
    void createTechnicianReportDraft_Success() {
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockUserRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        assertEquals(userRequestId, response.getData().getUserRequestId());
        assertEquals(technicianId, response.getData().getTechnicianId());
        assertEquals("Test diagnosis", response.getData().getDiagnosis());
        assertEquals("Test action plan", response.getData().getActionPlan());

        verify(userRequestRepository).findById(userRequestId);
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void createTechnicianReportDraft_NullRequest() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("null"));

        verifyNoInteractions(userRequestRepository);
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void createTechnicianReportDraft_NullTechnician() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("null"));

        verifyNoInteractions(userRequestRepository);
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void createTechnicianReportDraft_EmptyUserRequestId() {
        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
        request.setUserRequestId("");
        request.setDiagnosis("Test diagnosis");
        request.setActionPlan("Test action plan");
        request.setEstimatedCost(new BigDecimal("100.00"));
        request.setEstimatedTimeSeconds(3600L);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("User request ID"));
    }

    @Test
    void createTechnicianReportDraft_UserRequestNotFound() {
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(userRequestRepository).findById(any(UUID.class));
    }

    @Test
    void createTechnicianReportDraft_DatabaseException() {
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockUserRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(userRequestRepository).findById(any(UUID.class));
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void updateTechnicianReportDraft_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        CreateTechnicianReportDraft updateRequest = new CreateTechnicianReportDraft();
        updateRequest.setDiagnosis("Updated diagnosis");
        updateRequest.setActionPlan("Updated action plan");
        updateRequest.setEstimatedCost(new BigDecimal("150.00"));
        updateRequest.setEstimatedTimeSeconds(7200L);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), updateRequest, technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void updateTechnicianReportDraft_NotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void updateTechnicianReportDraft_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), mockCreateRequest, differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void updateTechnicianReportDraft_NotDraftState() {
        mockTechnicianReport.submit(); // Change state from DRAFT to SUBMITTED

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only report drafts"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void deleteTechnicianReportDraft_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        doNothing().when(technicianReportRepository).delete(any(TechnicianReport.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        assertEquals(technicianId, response.getData().getTechnicianId());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).delete(mockTechnicianReport);
    }

    @Test
    void deleteTechnicianReportDraft_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void acceptTechnicianReportSubmit_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        mockTechnicianReport.submit();

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), customer);

        assertTrue(response.isSuccess());
        assertEquals("APPROVED", mockTechnicianReport.getStatus());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void acceptTechnicianReportSubmit_UnauthorizedCustomer() {
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = mock(AuthenticatedUser.class);
        when(differentCustomer.id()).thenReturn(differentCustomerId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), differentCustomer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void rejectTechnicianReportSubmit_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        mockTechnicianReport.submit();
        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), customer);

        assertTrue(response.isSuccess());
        assertEquals("REJECTED", mockTechnicianReport.getStatus());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void getDraftReportsForTechnician_Success() {
        List<TechnicianReport> mockReports = Arrays.asList(mockTechnicianReport);
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(UUID.class), eq("DRAFT")))
                .thenReturn(mockReports);

        List<TechnicianReport> result = technicianReportService.getDraftReportsForTechnician(technician);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reportId, result.get(0).getReportId());

        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, "DRAFT");
    }

    @Test
    void getDraftReportsForTechnician_NullTechnician() {
        assertThrows(InvalidTechnicianReportStateException.class, () ->
                technicianReportService.getDraftReportsForTechnician(null));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void getDraftReportsForTechnician_DatabaseException() {
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(UUID.class), anyString()))
                .thenThrow(mock(DataAccessException.class));

        assertThrows(DatabaseException.class, () ->
                technicianReportService.getDraftReportsForTechnician(technician));

        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, "DRAFT");
    }

    @Test
    void submitTechnicianReportDraft_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("SUBMITTED", mockTechnicianReport.getStatus());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void submitTechnicianReportDraft_NullReportId() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_NullTechnician() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("technician"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_InvalidReportIdFormat() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft("invalid-uuid", technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Invalid UUID"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void submitTechnicianReportDraft_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = new AuthenticatedUser(
                differentTechnicianId,
                "another@example.com",
                "Another Technician",
                UserRole.TECHNICIAN,
                "98765432101",
                Instant.now(),
                Instant.now(),
                "Bandung",
                null,
                0,
                0L
        );

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void submitTechnicianReportDraft_NotInDraftState() {
        mockTechnicianReport.submit(); // Already in SUBMITTED state

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only report drafts"));
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void submitTechnicianReportDraft_DatabaseException() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void getTechnicianReportByStatus_Success() {
        String status = "DRAFT";
        List<TechnicianReport> mockReports = Arrays.asList(mockTechnicianReport);
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(technicianId, status))
                .thenReturn(mockReports);

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatus(status, technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(reportId, response.getData().get(0).getReportId());
        assertEquals("Technician reports retrieved successfully", response.getMessage());

        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, status);
    }

    @Test
    void getTechnicianReportByStatus_NullTechnician() {
        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatus("DRAFT", null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void getTechnicianReportByStatus_EmptyReportsList() {
        String status = "DRAFT";
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(technicianId, status))
                .thenReturn(Collections.emptyList());

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatus(status, technician);

        assertTrue(response.isSuccess());
        assertEquals(response.getData(), Collections.emptyList());


        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, status);
    }

    @Test
    void getTechnicianReportByStatus_DatabaseException() {
        String status = "DRAFT";
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(UUID.class), anyString()))
                .thenThrow(mock(DataAccessException.class));

        assertThrows(DatabaseException.class, () ->
                technicianReportService.getTechnicianReportByStatus(status, technician));

        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, status);
    }

    @Test
    void getTechnicianReportSubmissions_Success() {
        String status = "SUBMITTED";
        mockTechnicianReport.submit();
        List<TechnicianReport> mockReports = Arrays.asList(mockTechnicianReport);

        when(technicianReportRepository.findAllByStatus(status)).thenReturn(mockReports);

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportSubmissions(status, customer);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("Technician report submissions retrieved successfully", response.getMessage());

        verify(technicianReportRepository).findAllByStatus(status);
    }

    @Test
    void getTechnicianReportSubmissions_NullCustomer() {
        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportSubmissions("SUBMITTED", null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Customer cannot be null"));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void getTechnicianReportSubmissions_EmptyReportsList() {
        String status = "SUBMITTED";
        when(technicianReportRepository.findAllByStatus(status))
                .thenReturn(Collections.emptyList());

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportSubmissions(status, customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("No technician report submissions found", response.getMessage());

        verify(technicianReportRepository).findAllByStatus(status);
    }

    @Test
    void getTechnicianReportSubmissions_DatabaseException() {
        String status = "SUBMITTED";
        when(technicianReportRepository.findAllByStatus(anyString()))
                .thenThrow(mock(DataAccessException.class));

        assertThrows(DatabaseException.class, () ->
                technicianReportService.getTechnicianReportSubmissions(status, customer));

        verify(technicianReportRepository).findAllByStatus(status);
    }

    @Test
    void startWorkTechnicianReportAccepted() {
        mockTechnicianReport.submit(); // Change state from DRAFT to SUBMITTED
        mockTechnicianReport.approve(); // Change state from SUBMITTED to ACCEPTED
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertEquals("IN_PROGRESS", mockTechnicianReport.getStatus());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void startWorkTechnicianReportWithoutApproval() {
        mockTechnicianReport.submit(); // Change state from DRAFT to SUBMITTED
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only report Approved one can be started"));

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void startWorkTechnicianReportWithReportIdNull() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Report data or technician cannot be null"));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void startWorkTechnicianReportWithTechnicianNull() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Report data or technician cannot be null"));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void startWorkTechnicianReportButTechnicianNotAuthorized() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = new AuthenticatedUser(
                differentTechnicianId,
                "danniel@gmail.com",
                "Danniel",
                UserRole.TECHNICIAN,
                "1234567890",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), differentTechnician);
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

}