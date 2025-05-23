package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.messaging.RepairEventPublisher;
import id.ac.ui.cs.advprog.everest.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.*;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.InvalidTechnicianReportStateException;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianReportServiceImplTest {

    @Mock(lenient = true)
    private TechnicianReportAuditLogger auditLogger;

    @Mock
    private TechnicianReportRepository technicianReportRepository;

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private RepairEventPublisher repairEventPublisher;

    @InjectMocks
    private TechnicianReportServiceImpl technicianReportService;

    private UUID reportId;
    private UUID repairOrderId;
    private UUID technicianId;
    private UUID customerId;
    private AuthenticatedUser technician;
    private AuthenticatedUser customer;
    private CreateTechnicianReportDraftRequest mockCreateRequest;
    private TechnicianReport mockTechnicianReport;
    private RepairOrder mockRepairOrder;

    @BeforeEach
    void setUp() {
        doNothing().when(auditLogger).logReportAction(anyString(), anyString(), anyString());

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

        mockCreateRequest = CreateTechnicianReportDraftRequest.builder().
                repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

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
                .build();
    }

    @Test
    void CreateTechnicianReportDraftRequest_Success() {
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockRepairOrder));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        assertEquals(repairOrderId, response.getData().getRepairOrderId());
        assertEquals(technicianId, response.getData().getTechnicianId());
        assertEquals("Test diagnosis", response.getData().getDiagnosis());
        assertEquals("Test action plan", response.getData().getActionPlan());

        verify(repairOrderRepository).findById(repairOrderId);
        verify(technicianReportRepository).save(any(TechnicianReport.class));
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_RepairOrderNotFound() {
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));

        verify(repairOrderRepository).findById(repairOrderId);
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_DatabaseException() {
        when(repairOrderRepository.findById(any(UUID.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());

        verify(repairOrderRepository).findById(repairOrderId);
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_RepairOrderNotAssignedToTechnician() {
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockRepairOrder));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));

    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_NullRequest() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().toLowerCase().contains("null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_EmptyRepairOrderId() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId("")
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().toLowerCase().contains("null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_RepairOrderNotInPendingConfirmation() {
        RepairOrder notPending = RepairOrder.builder()
                .id(repairOrderId)
                .customerId(customerId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.IN_PROGRESS)
                .build();
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(notPending));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not in progress"));
        verify(repairOrderRepository).findById(repairOrderId);
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_NegativeEstimatedTime() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(-1L)
                .build();
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        System.out.println(response.getMessage());
        assertTrue(response.getMessage().contains("positive"));
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_NegativeEstimatedCost() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(-1L)
                .estimatedTimeSeconds(3600L)
                .build();
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null or negative"));
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_EmptyDiagnosis() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("diagnosis"));
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_EmptyActionPlan() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("actionPlan cannot be null"));
    }

    @Test
    void CreateTechnicianReportDraftRequest_Failed_ReportAlreadyExists() {
        when(repairOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockRepairOrder));
        when(technicianReportRepository.findAllByRepairOrderId(any(UUID.class))).thenReturn(List.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.createTechnicianReportDraft(mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Report already exists"));
        verify(repairOrderRepository).findById(repairOrderId);
    }

    @Test
    void updateTechnicianReportDraft_Success() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        CreateTechnicianReportDraftRequest updateRequest = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Updated diagnosis")
                .actionPlan("Updated action plan")
                .estimatedCost(150L)
                .estimatedTimeSeconds(7200L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), updateRequest, technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Updated diagnosis", response.getData().getDiagnosis());
        assertEquals("Updated action plan", response.getData().getActionPlan());
        assertEquals(150L, response.getData().getEstimatedCost());
        assertEquals(7200L, response.getData().getEstimatedTimeSeconds());

        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void updateTechnicianReportDraft_Failed_ReportIdNull() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(null, mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().toLowerCase().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void updateTechnicianReportDraft_Failed_RequestNull() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().toLowerCase().contains("null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void updateTechnicianReportDraft_Failed_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(any(UUID.class));
    }

    @Test
    void updateTechnicianReportDraft_Failed_UnauthorizedTechnician() {
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
    void updateTechnicianReportDraft_Failed_NotDraftState() {
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
    void updateTechnicianReportDraft_Failed_NegativeEstimatedTime() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(-1L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("positive"));
    }

    @Test
    void updateTechnicianReportDraft_Failed_NegativeEstimatedCost() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("Test action plan")
                .estimatedCost(-1L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("negative"));
    }

    @Test
    void updateTechnicianReportDraft_Failed_EmptyDiagnosis() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("")
                .actionPlan("Test action plan")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("diagnosis"));
    }

    @Test
    void updateTechnicianReportDraft_Failed_EmptyActionPlan() {
        CreateTechnicianReportDraftRequest request = CreateTechnicianReportDraftRequest.builder()
                .repairOrderId(repairOrderId.toString())
                .diagnosis("Test diagnosis")
                .actionPlan("")
                .estimatedCost(100L)
                .estimatedTimeSeconds(3600L)
                .build();

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), request, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("actionPlan"));
    }

    @Test
    void updateTechnicianReportDraft_Failed_DatabaseException() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.updateTechnicianReportDraft(reportId.toString(), mockCreateRequest, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
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
    void deleteTechnicianReportDraft_Failed_NullReportId() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void deleteTechnicianReportDraft_Failed_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void deleteTechnicianReportDraft_Failed_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void deleteTechnicianReportDraft_Failed_NotDraftState() {
        mockTechnicianReport.submit(); // Change state from DRAFT to SUBMITTED

        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only report drafts"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void deleteTechnicianReportDraft_Failed_DatabaseException() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.deleteTechnicianReportDraft(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
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
    void submitTechnicianReportDraft_InvalidReportIdFormat() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.submitTechnicianReportDraft("invalid-uuid", technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().toLowerCase().contains("invalid uuid"));
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
    void acceptTechnicianReportSubmit_Success() {
        mockTechnicianReport.submit(); // Change state from DRAFT to SUBMITTED
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), customer);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Technician report draft accepted successfully", response.getMessage());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void acceptTechnicianReportSubmit_NullReportId() {
        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(null, customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void acceptTechnicianReportSubmit_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void acceptTechnicianReportSubmit_UnauthorizedCustomer() {
        mockTechnicianReport.submit();
        AuthenticatedUser anotherCustomer = new AuthenticatedUser(
                UUID.randomUUID(), "other@example.com", "Other", UserRole.CUSTOMER,
                "0000000000", Instant.now(), Instant.now(), "Jakarta", null, 0, 0L
        );
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), anotherCustomer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void acceptTechnicianReportSubmit_NotSubmittedState() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not in submitted state"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void acceptTechnicianReportSubmit_DatabaseException() {
        mockTechnicianReport.submit();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<Void> response =
                technicianReportService.acceptTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void rejectTechnicianReportSubmit_Success() {
        mockTechnicianReport.submit();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), customer);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Technician report draft rejected successfully", response.getMessage());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void rejectTechnicianReportSubmit_NullReportId() {
        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(null, customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void rejectTechnicianReportSubmit_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void rejectTechnicianReportSubmit_UnauthorizedCustomer() {
        mockTechnicianReport.submit();
        AuthenticatedUser anotherCustomer = new AuthenticatedUser(
                UUID.randomUUID(), "other@example.com", "Other", UserRole.CUSTOMER,
                "0000000000", Instant.now(), Instant.now(), "Jakarta", null, 0, 0L
        );
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), anotherCustomer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void rejectTechnicianReportSubmit_NotSubmittedState() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not in submitted state"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void rejectTechnicianReportSubmit_DatabaseException() {
        mockTechnicianReport.submit();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<Void> response =
                technicianReportService.rejectTechnicianReportSubmit(reportId.toString(), customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void startWork_Success() {
        mockTechnicianReport.submit();
        mockTechnicianReport.approve(); // Set status to APPROVED
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("IN_PROGRESS", mockRepairOrder.getStatus().name());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void startWork_Failed_NullReportId() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void startWork_Failed_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void startWork_Failed_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);
        mockTechnicianReport.submit();
        mockTechnicianReport.approve();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void startWork_Failed_NotApprovedState() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only approved reports"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void startWork_Failed_DatabaseException() {
        mockTechnicianReport.submit();
        mockTechnicianReport.approve();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.startWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void completeWork_Success() {
        mockTechnicianReport.submit();
        mockTechnicianReport.approve();
        mockTechnicianReport.startWork(); // Set status to IN_PROGRESS
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(mockTechnicianReport);
        doNothing().when(repairEventPublisher).publishRepairCompleted(any(RepairOrderCompletedEvent.class));
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("COMPLETED", mockTechnicianReport.getStatus()); // Status before complete
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void completeWork_Failed_NullReportId() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void completeWork_Failed_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void completeWork_Failed_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);
        mockTechnicianReport.submit();
        mockTechnicianReport.approve();
        mockTechnicianReport.startWork();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void completeWork_Failed_NotInProgressState() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Only reports in progress"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void completeWork_Failed_DatabaseException() {
        mockTechnicianReport.submit();
        mockTechnicianReport.approve();
        mockTechnicianReport.startWork();
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.completeWork(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
        verify(technicianReportRepository).save(mockTechnicianReport);
    }

    @Test
    void getTechnicianReportByStatusForTechnician_Success() {
        List<TechnicianReport> reports = List.of(mockTechnicianReport);
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(eq(technicianId), anyString()))
                .thenReturn(reports);

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatusForTechnician("DRAFT", technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(reportId, response.getData().get(0).getReportId());
        verify(technicianReportRepository).findAllByTechnicianIdAndStatus(technicianId, "DRAFT");
    }

    @Test
    void getTechnicianReportByStatusForTechnician_Failed_DatabaseException() {
        when(technicianReportRepository.findAllByTechnicianIdAndStatus(any(), anyString()))
                .thenThrow(mock(DataAccessException.class));

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatusForTechnician("DRAFT", technician);

        assertFalse(response.isSuccess());
    }

    @Test
    void getTechnicianReportByStatusForCustomer_Success() {
        List<TechnicianReport> reports = List.of(mockTechnicianReport);
        when(technicianReportRepository.findAllByStatus(anyString())).thenReturn(reports);

        mockTechnicianReport.submit();
        mockTechnicianReport.approve();

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatusForCustomer("SUBMITTED", customer);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(reportId, response.getData().get(0).getReportId());
        verify(technicianReportRepository).findAllByStatus("SUBMITTED");
    }

    @Test
    void getTechnicianReportByStatusForCustomer_Failed_DraftStatus() {
        assertThrows(InvalidTechnicianReportStateException.class, () ->
                technicianReportService.getTechnicianReportByStatusForCustomer("DRAFT", customer));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void getTechnicianReportByStatusForCustomer_Failed_NoReportsFound() {
        when(technicianReportRepository.findAllByStatus(anyString())).thenReturn(List.of());

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatusForCustomer("SUBMITTED", customer);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("No technician report submissions found"));
        verify(technicianReportRepository).findAllByStatus("SUBMITTED");
    }

    @Test
    void getTechnicianReportByStatusForCustomer_Failed_DatabaseException() {
        when(technicianReportRepository.findAllByStatus(anyString()))
                .thenThrow(mock(DataAccessException.class));

        GenericResponse<List<TechnicianReportDraftResponse>> response =
                technicianReportService.getTechnicianReportByStatusForCustomer("SUBMITTED", customer);

        assertFalse(response.isSuccess());
    }

    @Test
    void getTechnicianReportById_Success_Technician() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.getTechnicianReportById(reportId.toString(), technician);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(reportId, response.getData().getReportId());
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void getTechnicianReportById_Failed_NullReportId() {
        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.getTechnicianReportById(null, technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("cannot be null"));
        verifyNoInteractions(technicianReportRepository);
    }

    @Test
    void getTechnicianReportById_Failed_ReportNotFound() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.empty());

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.getTechnicianReportById(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not found"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void getTechnicianReportById_Failed_UnauthorizedTechnician() {
        UUID differentTechnicianId = UUID.randomUUID();
        AuthenticatedUser differentTechnician = mock(AuthenticatedUser.class);
        when(differentTechnician.id()).thenReturn(differentTechnicianId);
        when(differentTechnician.role()).thenReturn(UserRole.TECHNICIAN);
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenReturn(Optional.of(mockTechnicianReport));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.getTechnicianReportById(reportId.toString(), differentTechnician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("not authorized"));
        verify(technicianReportRepository).findByReportId(reportId);
    }

    @Test
    void getTechnicianReportById_Failed_DatabaseException() {
        when(technicianReportRepository.findByReportId(any(UUID.class))).thenThrow(mock(DataAccessException.class));

        GenericResponse<TechnicianReportDraftResponse> response =
                technicianReportService.getTechnicianReportById(reportId.toString(), technician);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        verify(technicianReportRepository).findByReportId(reportId);
    }

}