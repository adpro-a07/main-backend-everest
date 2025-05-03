package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.repository.PaymentMethodRepository;
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

        sampleMethod = new PaymentMethod();
        sampleMethod.setId(UUID.randomUUID());
        sampleMethod.setName("Transfer BCA");
        sampleMethod.setType(PaymentType.BANK_TRANSFER);
        sampleMethod.setProvider("BCA");
        sampleMethod.setAccountNumber("1234567890");
        sampleMethod.setAccountName("PT Perbaikiin Aja");
    }

    @Test
    void testGetAllPaymentMethods() {
        List<PaymentMethod> mockList = List.of(sampleMethod);
        when(repository.findAll()).thenReturn(mockList);

        List<PaymentMethod> result = service.getAllPaymentMethods();

        assertEquals(1, result.size());
        assertEquals("Transfer BCA", result.get(0).getName());
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
        PaymentMethod newData = new PaymentMethod();
        newData.setName("Dana");
        newData.setType(PaymentType.E_WALLET);
        newData.setProvider("Dana");
        newData.setAccountNumber("9876543210");
        newData.setAccountName("PT Perbaikiin Aja");

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
        PaymentMethod newData = new PaymentMethod();

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
        PaymentMethod invalid = new PaymentMethod();
        invalid.setName("Transfer BRI");
        invalid.setType(PaymentType.BANK_TRANSFER);
        invalid.setProvider("BRI");
        invalid.setAccountNumber("123"); // Too short
        invalid.setAccountName("PT Perbaikiin Aja");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.save(invalid);
        });

        assertEquals("Bank account number must be 10–16 digits", exception.getMessage());
    }

    @Test
    void testSavePaymentMethodNullFields() {
        PaymentMethod invalid = new PaymentMethod();
        // Missing all fields

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.save(invalid);
        });

        assertEquals("All fields must be non-null", exception.getMessage());
    }
}