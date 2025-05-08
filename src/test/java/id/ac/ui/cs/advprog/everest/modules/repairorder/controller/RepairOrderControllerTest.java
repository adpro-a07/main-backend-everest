package id.ac.ui.cs.advprog.everest.modules.repairorder.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.service.RepairOrderService;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepairOrderControllerTest {

    private RepairOrderService repairOrderService;
    private RepairOrderController controller;

    @BeforeEach
    void setUp() {
        repairOrderService = mock(RepairOrderService.class);
        controller = new RepairOrderController(repairOrderService);
    }

    @Test
    void whenCreateRepairOrder_withValidRequest_shouldReturn201Created() {
        // Arrange
        CreateRepairOrderRequest request = new CreateRepairOrderRequest();
        // Fill request with valid values
        AuthenticatedUser user = new AuthenticatedUser(
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
        GenericResponse<Void> expectedResponse = new GenericResponse<>(
                true,
                "Repair order created successfully",
                null
        );

        when(repairOrderService.createRepairOrder(request, user)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.createRepairOrder(request, user);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenCreateRepairOrder_withServiceThrowsException_shouldThrow() {
        // Arrange
        CreateRepairOrderRequest request = new CreateRepairOrderRequest();
        AuthenticatedUser user = new AuthenticatedUser(
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

        when(repairOrderService.createRepairOrder(request, user)).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.createRepairOrder(request, user));
        assertEquals("Service failure", ex.getMessage());
    }
}

