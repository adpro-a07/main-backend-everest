package id.ac.ui.cs.advprog.everest.modules.repairorder.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateAndUpdateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.service.RepairOrderService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepairOrderControllerTest {

    private RepairOrderService repairOrderService;
    private RepairOrderController controller;
    private AuthenticatedUser user;

    @BeforeEach
    void setUp() {
        repairOrderService = mock(RepairOrderService.class);
        controller = new RepairOrderController(repairOrderService);
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
    }

    @Test
    void whenCreateRepairOrder_withValidRequest_shouldReturn201Created() {
        CreateAndUpdateRepairOrderRequest request = new CreateAndUpdateRepairOrderRequest();
        GenericResponse<ViewRepairOrderResponse> expectedResponse = new GenericResponse<>(
                true,
                "Repair order created successfully",
                null
        );

        when(repairOrderService.createRepairOrder(request, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.createRepairOrder(request, user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenCreateRepairOrder_withServiceThrowsException_shouldThrow() {
        CreateAndUpdateRepairOrderRequest request = new CreateAndUpdateRepairOrderRequest();
        when(repairOrderService.createRepairOrder(request, user)).thenThrow(new RuntimeException("Service failure"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.createRepairOrder(request, user));
        assertEquals("Service failure", ex.getMessage());
    }

    @Test
    void whenGetRepairOrders_shouldReturnList() {
        GenericResponse<List<ViewRepairOrderResponse>> expectedResponse = new GenericResponse<>(
                true,
                "Success",
                Collections.emptyList()
        );

        when(repairOrderService.getRepairOrders(user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.getRepairOrders(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenGetRepairOrders_unauthorized_shouldThrowAccessDenied() {
        when(repairOrderService.getRepairOrders(user)).thenThrow(new AccessDeniedException("Forbidden"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> controller.getRepairOrders(user));
        assertEquals("Forbidden", ex.getMessage());
    }

    @Test
    void whenUpdateRepairOrder_shouldReturnUpdatedOrder() {
        String repairOrderId = "order123";
        CreateAndUpdateRepairOrderRequest request = new CreateAndUpdateRepairOrderRequest();
        GenericResponse<ViewRepairOrderResponse> expectedResponse = new GenericResponse<>(
                true,
                "Updated",
                null
        );

        when(repairOrderService.updateRepairOrder(repairOrderId, request, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.updateRepairOrder(repairOrderId, request, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenUpdateRepairOrder_notFound_shouldThrow() {
        String repairOrderId = "nonexistent-id";
        CreateAndUpdateRepairOrderRequest request = new CreateAndUpdateRepairOrderRequest();

        when(repairOrderService.updateRepairOrder(repairOrderId, request, user))
                .thenThrow(new IllegalArgumentException("Repair order not found"));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateRepairOrder(repairOrderId, request, user));
        assertEquals("Repair order not found", ex.getMessage());
    }

    @Test
    void whenDeleteRepairOrder_shouldReturnSuccess() {
        String repairOrderId = "order123";
        GenericResponse<Void> expectedResponse = new GenericResponse<>(
                true,
                "Deleted",
                null
        );

        when(repairOrderService.deleteRepairOrder(repairOrderId, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.deleteRepairOrder(repairOrderId, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenDeleteRepairOrder_notFound_shouldThrow() {
        String repairOrderId = "bad-id";

        when(repairOrderService.deleteRepairOrder(repairOrderId, user))
                .thenThrow(new IllegalArgumentException("Repair order not found"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.deleteRepairOrder(repairOrderId, user));
        assertEquals("Repair order not found", ex.getMessage());
    }

    @Test
    void whenDeleteRepairOrder_unauthorized_shouldThrowAccessDenied() {
        String repairOrderId = "unauthorized-id";

        when(repairOrderService.deleteRepairOrder(repairOrderId, user))
                .thenThrow(new AccessDeniedException("Not allowed"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> controller.deleteRepairOrder(repairOrderId, user));
        assertEquals("Not allowed", ex.getMessage());
    }
}
