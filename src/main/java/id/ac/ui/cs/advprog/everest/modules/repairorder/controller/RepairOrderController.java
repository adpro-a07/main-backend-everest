package id.ac.ui.cs.advprog.everest.modules.repairorder.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.service.RepairOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RepairOrderController {
    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/repair-orders")
    public ResponseEntity<?> createRepairOrder(@Valid @RequestBody CreateRepairOrderRequest createRepairOrderRequest,
                                               @CurrentUser AuthenticatedUser user) {
        GenericResponse<Void> response = repairOrderService.createRepairOrder(createRepairOrderRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
