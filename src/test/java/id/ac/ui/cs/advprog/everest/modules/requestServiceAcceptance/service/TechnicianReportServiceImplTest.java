package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidTechnicianReportStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianReportServiceImplTest {

    @Mock
    private TechnicianReportRepository technicianReportRepository;

    @Mock
    private UserRequestRepository userRequestRepository;

    @InjectMocks
    private TechnicianReportServiceImpl technicianReportService;

    private AuthenticatedUser technician;
    private AuthenticatedUser customer;
    private UserRequest mockUserRequest;
    private TechnicianReport mockTechnicianReport;
    private CreateTechnicianReportDraft mockCreateRequest;
    private UUID technicianId;
    private UUID customerId;
    private UUID userRequestId;
    private UUID reportId;

    @BeforeEach
    void setUp() {
        // Set up UUIDs
        technicianId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userRequestId = UUID.randomUUID();
        reportId = UUID.randomUUID();

        // Set up AuthenticatedUser objects (not mocks)
        technician = new AuthenticatedUser(
                technicianId,
                "technician@example.com",
                "Technician",
                UserRole.TECHNICIAN,
                "12345678901",
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

        // Set up UserRequest
        mockUserRequest = new UserRequest();
        mockUserRequest.setRequestId(userRequestId);
        mockUserRequest.setUserId(customerId);
        mockUserRequest.setUserDescription("Test user request description");

        // Set up TechnicianReport
        mockTechnicianReport = TechnicianReport.builder()
                .reportId(reportId)
                .userRequest(mockUserRequest)
                .technicianId(technicianId)
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofMinutes(60))
                .build();

        // Set up CreateTechnicianReportDraft request
        mockCreateRequest = new CreateTechnicianReportDraft();
        mockCreateRequest.setUserRequestId(userRequestId.toString());
        mockCreateRequest.setDiagnosis("Test diagnosis");
        mockCreateRequest.setActionPlan("Test action plan");
        mockCreateRequest.setEstimatedCost(new BigDecimal("100.00"));
        mockCreateRequest.setEstimatedTimeSeconds(3600L);
    }

    @Test
    void createTechnicianReportDraft_Success() {
        // Arrange
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockUserRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        // Act
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        assertEquals(technicianId, response.getData().getTechnicianId());
        assertEquals("Test diagnosis", response.getData().getDiagnosis());
        assertEquals("Test action plan", response.getData().getActionPlan());
        assertEquals(new BigDecimal("100.00"), response.getData().getEstimatedCost());

        // Verify repository interactions
        verify(userRequestRepository).findById(UUID.fromString(mockCreateRequest.getUserRequestId()));
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void createTechnicianReportDraft_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.createTechnicianReportDraft(null, technician));
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.createTechnicianReportDraft(mockCreateRequest, null));
    }

    @Test
    void createTechnicianReportDraft_EmptyUserRequestId_ThrowsException() {
        // Arrange
        CreateTechnicianReportDraft request = new CreateTechnicianReportDraft();
        request.setUserRequestId("");
        request.setDiagnosis("Test diagnosis");
        request.setActionPlan("Test action plan");
        request.setEstimatedCost(new BigDecimal("100.00"));
        request.setEstimatedTimeSeconds(3600L);

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.createTechnicianReportDraft(request, technician));
    }

    @Test
    void createTechnicianReportDraft_UserRequestNotFound_ThrowsException() {
        // Arrange
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician));

        verify(userRequestRepository).findById(any(UUID.class));
    }

    @Test
    void createTechnicianReportDraft_DatabaseException_ThrowsException() {
        // Arrange
        when(userRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockUserRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class,
                () -> technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician));

        verify(userRequestRepository).findById(any(UUID.class));
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void updateTechnicianReportDraft_Success() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        // Update draft with new details
        CreateTechnicianReportDraft updateRequest = new CreateTechnicianReportDraft();
        updateRequest.setDiagnosis("Updated diagnosis");
        updateRequest.setActionPlan("Updated action plan");
        updateRequest.setEstimatedCost(new BigDecimal("150.00"));
        updateRequest.setEstimatedTimeSeconds(7200L);

        // Act
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), updateRequest, technician);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Updated diagnosis", mockTechnicianReport.getDiagnosis());
        assertEquals("Updated action plan", mockTechnicianReport.getActionPlan());
        assertEquals(new BigDecimal("150.00"), mockTechnicianReport.getEstimatedCost());
        assertEquals(7200L, mockTechnicianReport.getEstimatedTimeSeconds());

        // Verify repository interactions
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void updateTechnicianReportDraft_NotFound_ThrowsException() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.updateTechnicianReportDraft(
                        reportId.toString(), mockCreateRequest, technician));

        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void updateTechnicianReportDraft_UnauthorizedTechnician_ThrowsException() {
        // Arrange
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.updateTechnicianReportDraft(
                        reportId.toString(), mockCreateRequest, differentTechnician));

        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void deleteTechnicianReportDraft_Success() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        doNothing().when(technicianReportRepository).delete(any(TechnicianReport.class));

        // Act
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), technician);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        assertEquals(technicianId, response.getData().getTechnicianId());

        // Verify repository interactions
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).delete(mockTechnicianReport);
    }

    @Test
    void deleteTechnicianReportDraft_UnauthorizedTechnician_ThrowsException() {
        // Arrange
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.deleteTechnicianReportDraft(reportId.toString(), differentTechnician));

        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void acceptTechnicianReportDraft_Success() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        mockTechnicianReport.submit();

        // Act
        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportDraft(reportId.toString(), customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("APPROVED", mockTechnicianReport.getStatus());

        // Verify repository interactions
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void acceptTechnicianReportDraft_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = mock(AuthenticatedUser.class);
        when(differentCustomer.id()).thenReturn(differentCustomerId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.acceptTechnicianReportDraft(reportId.toString(), differentCustomer));

        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void rejectTechnicianReportDraft_Success() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        mockTechnicianReport.submit();
        // Act
        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportDraft(reportId.toString(), customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("REJECTED", mockTechnicianReport.getStatus());

        // Verify repository interactions
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void getDraftReportsForTechnician_Success() {
        // Arrange
        List<TechnicianReport> mockReports = Arrays.asList(mockTechnicianReport);
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(UUID.class), eq("DRAFT")))
                .thenReturn(mockReports);

        // Act
        List<TechnicianReport> result = technicianReportService.getDraftReportsForTechnician(technician);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reportId, result.get(0).getReportId());

        // Verify repository interactions
        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, "DRAFT");
    }

    @Test
    void getDraftReportsForTechnician_NullTechnician_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.getDraftReportsForTechnician(null));
    }

    @Test
    void getDraftReportsForTechnician_DatabaseException_ThrowsException() {
        // Arrange
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(UUID.class), eq("DRAFT")))
                .thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class,
                () -> technicianReportService.getDraftReportsForTechnician(technician));

        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(any(UUID.class), eq("DRAFT"));
    }

    @Test
    void submitTechnicianReportDraft_Success() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        // Act
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("SUBMITTED", mockTechnicianReport.getStatus());

        // Verify repository interactions
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void submitTechnicianReportDraft_NullReportId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft(null, technician));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_NullTechnician_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft(reportId.toString(), null));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_InvalidReportIdFormat_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft("invalid-uuid", technician));

        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void submitTechnicianReportDraft_ReportNotFound_ThrowsException() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician));

        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void submitTechnicianReportDraft_UnauthorizedTechnician_ThrowsException() {
        // Arrange
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

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft(reportId.toString(), differentTechnician));

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void submitTechnicianReportDraft_NotInDraftState_ThrowsException() {
        // Arrange
        mockTechnicianReport.submit(); // Already in SUBMITTED state

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        // Act & Assert
        assertThrows(InvalidTechnicianReportStateException.class,
                () -> technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician));

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void submitTechnicianReportDraft_DatabaseException_ThrowsException() {
        // Arrange
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class,
                () -> technicianReportService.submitTechnicianReportDraft(reportId.toString(), technician));

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }
}