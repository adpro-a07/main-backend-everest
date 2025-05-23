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
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GenericResponse<PaymentMethodDetailDto>> createPaymentMethod(
            @Valid @RequestBody CreateAndUpdatePaymentMethodRequest request
    ) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<GenericResponse<List<PaymentMethodSummaryDto>>> getAllPaymentMethods() {
        GenericResponse<List<PaymentMethodSummaryDto>> response = paymentMethodService.getAllPaymentMethods();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<PaymentMethodDetailDto>> readPaymentMethod(@PathVariable UUID id) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.readDetails(id);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<PaymentMethodDetailDto>> updatePaymentMethod(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAndUpdatePaymentMethodRequest request
    ) {
        GenericResponse<PaymentMethodDetailDto> response = paymentMethodService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deletePaymentMethod(@PathVariable UUID id) {
        GenericResponse<Void> response = paymentMethodService.delete(id);
        return ResponseEntity.ok(response);
    }
}
