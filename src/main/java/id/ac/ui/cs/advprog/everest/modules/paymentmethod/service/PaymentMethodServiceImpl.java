package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repository;

    public PaymentMethodServiceImpl(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> readDetails(UUID id) {
        try {
            PaymentMethod method = repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Payment method not found"));

            return new GenericResponse<>(true, "Payment method retrieved successfully", toDetailDto(method));
        } catch (NoSuchElementException | DataAccessException e) {
            return new GenericResponse<>(false, e.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<List<PaymentMethodSummaryDto>> getAllPaymentMethods() {
        try {
            List<PaymentMethod> methods = repository.findAll();
            List<PaymentMethodSummaryDto> dtoList = methods.stream()
                    .map(this::toSummaryDto)
                    .collect(Collectors.toList());
            return new GenericResponse<>(true, "Payment methods retrieved successfully", dtoList);
        } catch (DataAccessException e) {
            return new GenericResponse<>(false, "Failed to retrieve payment methods", null);
        }
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> create(CreateAndUpdatePaymentMethodRequest request) {
        try {
            PaymentMethod method = fromRequest(request);
            validate(method);
            PaymentMethod saved = repository.save(method);
            return new GenericResponse<>(true, "Payment method created successfully", toDetailDto(saved));
        } catch (IllegalArgumentException | DataAccessException e) {
            return new GenericResponse<>(false, e.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> update(UUID id, CreateAndUpdatePaymentMethodRequest request) {
        try {
            PaymentMethod existing = repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Payment method not found"));

            existing.setName(request.getName());
            existing.setType(request.getType());
            existing.setProvider(request.getProvider());
            existing.setAccountNumber(request.getAccountNumber());
            existing.setAccountName(request.getAccountName());

            validate(existing);
            PaymentMethod updated = repository.save(existing);
            return new GenericResponse<>(true, "Payment method updated successfully", toDetailDto(updated));
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            return new GenericResponse<>(false, e.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<Void> delete(UUID id) {
        try {
            repository.deleteById(id);
            return new GenericResponse<>(true, "Payment method deleted successfully", null);
        } catch (DataAccessException e) {
            return new GenericResponse<>(false, "Failed to delete payment method", null);
        }
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
        }
    }

    private PaymentMethod fromRequest(CreateAndUpdatePaymentMethodRequest request) {
        return PaymentMethod.builder()
                .name(request.getName())
                .type(request.getType())
                .provider(request.getProvider())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .build();
    }

    private PaymentMethodDetailDto toDetailDto(PaymentMethod method) {
        return PaymentMethodDetailDto.builder()
                .id(method.getId())
                .name(method.getName())
                .type(method.getType())
                .provider(method.getProvider())
                .accountNumber(method.getAccountNumber())
                .accountName(method.getAccountName())
                .createdAt(method.getCreatedAt())
                .updatedAt(method.getUpdatedAt())
                .build();
    }

    private PaymentMethodSummaryDto toSummaryDto(PaymentMethod method) {
        return PaymentMethodSummaryDto.builder()
                .id(method.getId())
                .name(method.getName())
                .type(method.getType())
                .provider(method.getProvider())
                .build();
    }


}
