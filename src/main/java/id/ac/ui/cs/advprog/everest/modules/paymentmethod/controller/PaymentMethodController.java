package id.ac.ui.cs.advprog.everest.modules.paymentmethod.controller;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/payment-methods")
    public ResponseEntity<?> createPaymentMethod(
            @Valid @RequestBody CreateAndUpdatePaymentMethodRequest request
    ) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getAllPaymentMethods() {
        GenericResponse<List<PaymentMethodSummaryDto>> response = paymentMethodService.getAllPaymentMethods();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payment-methods/{id}")
    public ResponseEntity<?> readPaymentMethod(@PathVariable UUID id) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.readDetails(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/payment-methods/{id}")
    public ResponseEntity<?> updatePaymentMethod(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAndUpdatePaymentMethodRequest request
    ) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/payment-methods/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable UUID id) {
        GenericResponse<Void> response = paymentMethodService.delete(id);
        return ResponseEntity.ok(response);
    }
}
