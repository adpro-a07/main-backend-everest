package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository repository;

    public PaymentMethodService(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    public List<PaymentMethod> getAllPaymentMethods() {
        return repository.findAll();
    }

    public PaymentMethod save(PaymentMethod method) {
        validate(method);
        return repository.save(method);
    }

    public PaymentMethod update(UUID id, PaymentMethod newData) {
        PaymentMethod existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Payment method not found"));

        existing.setName(newData.getName());
        existing.setType(newData.getType());
        existing.setProvider(newData.getProvider());
        existing.setAccountNumber(newData.getAccountNumber());
        existing.setAccountName(newData.getAccountName());

        validate(existing);
        return repository.save(existing);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private void validate(PaymentMethod method) {
        if (method.getName() == null || method.getType() == null ||
                method.getProvider() == null || method.getAccountNumber() == null ||
                method.getAccountName() == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

        int length = method.getAccountNumber().length();
        switch (method.getType()) {
            case BANK_TRANSFER -> {
                if (length < 10 || length > 16) {
                    throw new IllegalArgumentException("Bank account number must be 10–16 digits");
                }
            }
            case E_WALLET -> {
                if (length < 10 || length > 23) {
                    throw new IllegalArgumentException("Virtual account number must be 10–23 digits");
                }
            }
            default -> {

            }
        }
    }
}
