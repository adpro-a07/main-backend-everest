package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.repository.PaymentMethodRepository;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PaymentMethodService {


    private PaymentMethodRepository repository;

    public List<PaymentMethod> getAllPaymentMethods() {
        return repository.findAll();
    }

    public PaymentMethod save(PaymentMethod method) {
        return repository.save(method);
    }

    public PaymentMethod update(UUID id, PaymentMethod newData) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found"));
        method.setName(newData.getName());
        method.setType(newData.getType());
        method.setProvider(newData.getProvider());
        method.setAccountNumber(newData.getAccountNumber());
        method.setAccountName(newData.getAccountName());
        return repository.save(method);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
