package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentMethodServiceTest {

    @InjectMocks
    private PaymentMethodServiceImpl service;

    @Mock
    private PaymentMethodRepository repository;

    private CreateAndUpdatePaymentMethodRequest request;
    private PaymentMethod entity;
    private UUID id;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        id = UUID.randomUUID();
        request = new CreateAndUpdatePaymentMethodRequest();
        request.setName("Transfer BCA");
        request.setType(PaymentType.BANK_TRANSFER);
        request.setProvider("BCA");
        request.setAccountNumber("1234567890");
        request.setAccountName("PT Perbaikiin Aja");

        entity = PaymentMethod.builder()
                .id(id)
                .name(request.getName())
                .type(request.getType())
                .provider(request.getProvider())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreatePaymentMethod_Success() {
        when(repository.save(any(PaymentMethod.class))).thenReturn(entity);

        GenericResponse<PaymentMethodDetailDto> response = service.create(request);

        assertTrue(response.isSuccess());
        assertEquals("Transfer BCA", response.getData().getName());
        verify(repository).save(any());
    }

    @Test
    void testCreatePaymentMethod_InvalidAccountNumber() {
        request.setAccountNumber("123"); // too short for BANK_TRANSFER

        GenericResponse<PaymentMethodDetailDto> response = service.create(request);

        assertFalse(response.isSuccess());
        assertEquals("Bank account number must be 10–16 digits", response.getMessage());
    }

    @Test
    void testGetAllPaymentMethods_Success() {
        when(repository.findAll()).thenReturn(List.of(entity));

        GenericResponse<List<PaymentMethodSummaryDto>> response = service.getAllPaymentMethods();

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Transfer BCA", response.getData().get(0).getName());
    }

    @Test
    void testReadPaymentMethod_Success() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        GenericResponse<PaymentMethodDetailDto> response = service.readDetails(id);

        assertTrue(response.isSuccess());
        assertEquals("Transfer BCA", response.getData().getName());
        verify(repository).findById(id);
    }

    @Test
    void testReadPaymentMethod_NotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        GenericResponse<PaymentMethodDetailDto> response = service.readDetails(id);

        assertFalse(response.isSuccess());
        assertEquals("Payment method not found", response.getMessage());
    }

    @Test
    void testUpdatePaymentMethod_Success() {
        CreateAndUpdatePaymentMethodRequest newRequest = new CreateAndUpdatePaymentMethodRequest();
        newRequest.setName("Dana");
        newRequest.setType(PaymentType.E_WALLET);
        newRequest.setProvider("Dana");
        newRequest.setAccountNumber("9876543210");
        newRequest.setAccountName("PT Perbaikiin Aja");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse<PaymentMethodDetailDto> response = service.update(id, newRequest);

        assertTrue(response.isSuccess());
        assertEquals("Dana", response.getData().getName());
        verify(repository).save(any());
    }

    @Test
    void testUpdatePaymentMethod_NotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        GenericResponse<PaymentMethodDetailDto> response = service.update(id, request);

        assertFalse(response.isSuccess());
        assertEquals("Payment method not found", response.getMessage());
    }

    @Test
    void testDeletePaymentMethod_Success() {
        doNothing().when(repository).deleteById(id);

        GenericResponse<Void> response = service.delete(id);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        verify(repository).deleteById(id);
    }
}
