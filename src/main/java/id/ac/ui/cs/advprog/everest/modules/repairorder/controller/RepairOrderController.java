package id.ac.ui.cs.advprog.everest.modules.repairorder.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateAndUpdateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.service.RepairOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RepairOrderController {
    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/repair-orders")
    public ResponseEntity<?> createRepairOrder(
            @Valid @RequestBody CreateAndUpdateRepairOrderRequest createAndUpdateRepairOrderRequest,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService
                .createRepairOrder(createAndUpdateRepairOrderRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/repair-orders")
    public ResponseEntity<?> getRepairOrders(@CurrentUser AuthenticatedUser user) {
        GenericResponse<List<ViewRepairOrderResponse>> response = repairOrderService.getRepairOrders(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/repair-orders/{repairOrderId}")
    public ResponseEntity<?> updateRepairOrder(
            @PathVariable String repairOrderId,
            @Valid @RequestBody CreateAndUpdateRepairOrderRequest createAndUpdateRepairOrderRequest,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<ViewRepairOrderResponse> response = repairOrderService
                .updateRepairOrder(repairOrderId, createAndUpdateRepairOrderRequest, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/repair-orders/{repairOrderId}")
    public ResponseEntity<?> deleteRepairOrder(
            @PathVariable String repairOrderId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = repairOrderService.deleteRepairOrder(repairOrderId, user);
        return ResponseEntity.ok(response);
    }
}
