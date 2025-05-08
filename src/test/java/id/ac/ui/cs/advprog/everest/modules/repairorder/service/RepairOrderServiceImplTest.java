package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.InvalidRepairOrderStateException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.TechnicianUnavailableException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.GetRandomTechnicianResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceImplTest {

    @Mock
    private UserServiceGrpcClient userServiceGrpcClient;

    @Mock
    private RepairOrderRepository repairOrderRepository;

    private RepairOrderServiceImpl repairOrderService;
    private AuthenticatedUser authenticatedUser;
    private CreateRepairOrderRequest validRequest;
    private GetRandomTechnicianResponse randomTechnicianResponse;
    private final UUID technicianUuid = UUID.randomUUID();
    private final UUID customerUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repairOrderService = new RepairOrderServiceImpl(userServiceGrpcClient, repairOrderRepository);

        // Setup authenticated user
        authenticatedUser = new AuthenticatedUser(
                customerUuid,
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

        // Setup valid request
        validRequest = new CreateRepairOrderRequest();
        validRequest.setItemName("Broken Laptop");
        validRequest.setItemCondition("Screen cracked");
        validRequest.setIssueDescription("Screen not working properly");
        validRequest.setDesiredServiceDate(LocalDate.now().plusDays(2));

        // Setup technician response
        UserIdentity technicianUserIdentity = UserIdentity.newBuilder()
                .setId(technicianUuid.toString())
                .setEmail("technician@example.com")
                .setFullName("Tech User")
                .build();

        UserData technicianData = UserData.newBuilder()
                .setIdentity(technicianUserIdentity)
                .build();

        randomTechnicianResponse = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(technicianData)
                .build();
    }

    @Test
    void testCreateRepairOrder_Success() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);

        // Act
        GenericResponse<Void> response = repairOrderService.createRepairOrder(validRequest, authenticatedUser);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Repair order created successfully", response.getMessage());
        assertNull(response.getData());

        // Verify interactions
        verify(userServiceGrpcClient, times(1)).getRandomTechnician();

        // Capture and verify the saved repair order
        ArgumentCaptor<RepairOrder> repairOrderCaptor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderRepository, times(1)).save(repairOrderCaptor.capture());

        RepairOrder capturedOrder = repairOrderCaptor.getValue();
        assertEquals(customerUuid, capturedOrder.getCustomerId());
        assertEquals(technicianUuid, capturedOrder.getTechnicianId());
        assertEquals(RepairOrderStatus.PENDING_CONFIRMATION, capturedOrder.getStatus());
        assertEquals(validRequest.getItemName(), capturedOrder.getItemName());
        assertEquals(validRequest.getItemCondition(), capturedOrder.getItemCondition());
        assertEquals(validRequest.getIssueDescription(), capturedOrder.getIssueDescription());
        assertEquals(validRequest.getDesiredServiceDate(), capturedOrder.getDesiredServiceDate());
    }

    @Test
    void testCreateRepairOrder_TechnicianUnavailable() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(
                GetRandomTechnicianResponse.newBuilder().build() // No technician data
        );

        // Act & Assert
        assertThrows(TechnicianUnavailableException.class, () ->
                repairOrderService.createRepairOrder(validRequest, authenticatedUser));

        // Verify interactions
        verify(userServiceGrpcClient, times(1)).getRandomTechnician();
        verify(repairOrderRepository, never()).save(any());
    }

    @Test
    void testCreateRepairOrder_InvalidTechnicianId() {
        // Arrange
        // Create a response with an invalid UUID
        UserIdentity invalidUserIdentity = UserIdentity.newBuilder()
                .setId("invalid-uuid")
                .build();

        UserData invalidTechnician = UserData.newBuilder()
                .setIdentity(invalidUserIdentity)
                .build();

        GetRandomTechnicianResponse invalidTechResponse = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(invalidTechnician)
                .build();

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(invalidTechResponse);

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, authenticatedUser));

        // Verify interactions
        verify(userServiceGrpcClient, times(1)).getRandomTechnician();
        verify(repairOrderRepository, never()).save(any());
    }

    @Test
    void testCreateRepairOrder_NullTechnicianUserIdentity() {
        // Arrange
        // Create a response with null identity
        UserData invalidTechnician = UserData.newBuilder()
                // No identity set
                .build();

        GetRandomTechnicianResponse invalidResponse = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(invalidTechnician)
                .build();

        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(invalidResponse);

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, authenticatedUser));

        // Verify no save attempt was made
        verify(repairOrderRepository, never()).save(any());
    }

    @Test
    void testCreateRepairOrder_DatabaseException() {
        // Arrange
        when(userServiceGrpcClient.getRandomTechnician()).thenReturn(randomTechnicianResponse);
        when(repairOrderRepository.save(any(RepairOrder.class))).thenThrow(
                mock(DataAccessException.class)
        );

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                repairOrderService.createRepairOrder(validRequest, authenticatedUser));

        // Verify interactions
        verify(userServiceGrpcClient, times(1)).getRandomTechnician();
        verify(repairOrderRepository, times(1)).save(any());
    }

    @Test
    void testCreateRepairOrder_NullRequest() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(null, authenticatedUser));

        // Verify no interactions with dependencies
        verifyNoInteractions(userServiceGrpcClient);
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void testCreateRepairOrder_NullCustomer() {
        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(validRequest, null));

        // Verify no interactions with dependencies
        verifyNoInteractions(userServiceGrpcClient);
        verifyNoInteractions(repairOrderRepository);
    }

    @Test
    void testCreateRepairOrder_InvalidRequestFields() {
        // Arrange
        CreateRepairOrderRequest invalidRequest = new CreateRepairOrderRequest();
        // Missing required fields

        // Act & Assert
        assertThrows(InvalidRepairOrderStateException.class, () ->
                repairOrderService.createRepairOrder(invalidRequest, authenticatedUser));

        // Verify interactions
        verify(userServiceGrpcClient, times(1)).getRandomTechnician();
        verify(repairOrderRepository, never()).save(any());
    }
}
