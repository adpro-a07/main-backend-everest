package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentMethodServiceTest {

    @InjectMocks
    private PaymentMethodService service;

    @Mock
    private PaymentMethodRepository repository;

    private PaymentMethod sampleMethod;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleMethod = PaymentMethod.builder()
                .name("Transfer BCA")
                .type(PaymentType.BANK_TRANSFER)
                .provider("BCA")
                .accountNumber("1234567890")
                .accountName("PT Perbaikiin Aja")
                .build();
    }

    @Test
    void testGetAllPaymentMethods() {
        List<PaymentMethod> mockList = List.of(sampleMethod);
        when(repository.findAll()).thenReturn(mockList);

        List<PaymentMethod> result = service.getAllPaymentMethods();

        assertEquals(1, result.size());
        assertEquals("Transfer BCA", result.getFirst().getName());
        verify(repository).findAll();
    }

    @Test
    void testSavePaymentMethod() {
        when(repository.save(sampleMethod)).thenReturn(sampleMethod);

        PaymentMethod result = service.save(sampleMethod);

        assertNotNull(result);
        assertEquals("Transfer BCA", result.getName());
        verify(repository).save(sampleMethod);
    }

    @Test
    void testUpdatePaymentMethodSuccess() {
        UUID id = sampleMethod.getId();
        PaymentMethod newData = PaymentMethod.builder()
                .name("Dana")
                .type(PaymentType.E_WALLET)
                .provider("Dana")
                .accountNumber("9876543210")
                .accountName("PT Perbaikiin Aja")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleMethod));
        when(repository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentMethod updated = service.update(id, newData);

        assertEquals("Dana", updated.getName());
        assertEquals(PaymentType.E_WALLET, updated.getType());
        verify(repository).findById(id);
        verify(repository).save(sampleMethod);
    }

    @Test
    void testUpdatePaymentMethodNotFound() {
        UUID id = UUID.randomUUID();

        PaymentMethod newData = PaymentMethod.builder()
                .name("Dana")
                .type(PaymentType.E_WALLET)
                .provider("Dana")
                .accountNumber("9876543210")
                .accountName("PT Perbaikiin Aja")
                .build();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            service.update(id, newData);
        });

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    void testDeletePaymentMethod() {
        UUID id = sampleMethod.getId();

        doNothing().when(repository).deleteById(id);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void testSavePaymentMethodInvalidAccountNumberForBankTransfer() {
        PaymentMethod invalid = PaymentMethod.builder()
                .name("Transfer BRI")
                .type(PaymentType.BANK_TRANSFER)
                .provider("BRI")
                .accountNumber("123")
                .accountName("PT Perbaikiin Aja")
                .build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.save(invalid);
        });

        assertEquals("Bank account number must be 10–16 digits", exception.getMessage());
    }

    @Test
    void testSavePaymentMethodNullFields() {
        // Using builder without setting any fields
        PaymentMethod invalid = PaymentMethod.builder().build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.save(invalid);
        });

        assertEquals("All fields must be non-null", exception.getMessage());
    }
}