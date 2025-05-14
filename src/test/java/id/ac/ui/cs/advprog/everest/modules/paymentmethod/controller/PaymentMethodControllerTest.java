package id.ac.ui.cs.advprog.everest.modules.paymentmethod.controller;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.service.PaymentMethodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentMethodControllerTest {

    private PaymentMethodService service;
    private PaymentMethodController controller;
    private UUID id;
    private CreateAndUpdatePaymentMethodRequest request;
    private PaymentMethodDetailDto detailDto;

    @BeforeEach
    void setUp() {
        service = mock(PaymentMethodService.class);
        controller = new PaymentMethodController(service);
        id = UUID.randomUUID();

        request = new CreateAndUpdatePaymentMethodRequest();
        request.setName("Transfer BCA");
        request.setType(PaymentType.BANK_TRANSFER);
        request.setProvider("BCA");
        request.setAccountNumber("1234567890");
        request.setAccountName("PT Perbaikiin Aja");

        detailDto = PaymentMethodDetailDto.builder()
                .id(id)
                .name("Transfer BCA")
                .type(PaymentType.BANK_TRANSFER)
                .provider("BCA")
                .accountNumber("1234567890")
                .accountName("PT Perbaikiin Aja")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void whenCreatePaymentMethod_shouldReturn201Created() {
        GenericResponse<PaymentMethodDetailDto> expected = new GenericResponse<>(true, "Created", detailDto);
        when(service.create(request)).thenReturn(expected);

        ResponseEntity<?> response = controller.createPaymentMethod(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenCreatePaymentMethodFails_shouldThrow() {
        when(service.create(request)).thenThrow(new RuntimeException("Fail"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.createPaymentMethod(request));
        assertEquals("Fail", ex.getMessage());
    }

    @Test
    void whenGetAllPaymentMethods_shouldReturn200OK() {
        PaymentMethodSummaryDto summary = PaymentMethodSummaryDto.builder()
                .id(id)
                .name("Transfer BCA")
                .type(PaymentType.BANK_TRANSFER)
                .provider("BCA")
                .build();

        GenericResponse<List<PaymentMethodSummaryDto>> expected =
                new GenericResponse<>(true, "List fetched", List.of(summary));

        when(service.getAllPaymentMethods()).thenReturn(expected);

        ResponseEntity<?> response = controller.getAllPaymentMethods();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenReadPaymentMethodExists_shouldReturn200OK() {
        GenericResponse<PaymentMethodDetailDto> expected = new GenericResponse<>(true, "Found", detailDto);
        when(service.readDetails(id)).thenReturn(expected);

        ResponseEntity<?> response = controller.readPaymentMethod(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenReadPaymentMethodNotFound_shouldReturn404() {
        GenericResponse<PaymentMethodDetailDto> expected = new GenericResponse<>(false, "Not found", null);
        when(service.readDetails(id)).thenReturn(expected);

        ResponseEntity<?> response = controller.readPaymentMethod(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenUpdatePaymentMethod_shouldReturn200OK() {
        GenericResponse<PaymentMethodDetailDto> expected = new GenericResponse<>(true, "Updated", detailDto);
        when(service.update(id, request)).thenReturn(expected);

        ResponseEntity<?> response = controller.updatePaymentMethod(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenUpdateFails_shouldThrow() {
        when(service.update(id, request)).thenThrow(new IllegalArgumentException("Invalid"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.updatePaymentMethod(id, request));
        assertEquals("Invalid", ex.getMessage());
    }

    @Test
    void whenDeletePaymentMethod_shouldReturn200OK() {
        GenericResponse<Void> expected = new GenericResponse<>(true, "Deleted", null);
        when(service.delete(id)).thenReturn(expected);

        ResponseEntity<?> response = controller.deletePaymentMethod(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void whenDeleteFails_shouldThrow() {
        when(service.delete(id)).thenThrow(new IllegalStateException("Cannot delete"));
        Exception ex = assertThrows(IllegalStateException.class, () -> controller.deletePaymentMethod(id));
        assertEquals("Cannot delete", ex.getMessage());
    }
}
