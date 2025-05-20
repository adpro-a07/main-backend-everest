package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository.PaymentMethodRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private static final String NOT_FOUND_MESSAGE = "Payment method not found";
    private final PaymentMethodRepository repository;

    public PaymentMethodServiceImpl(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> readDetails(UUID id) {
        return handle(() -> {
            PaymentMethod method = repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_MESSAGE));
            return new GenericResponse<>(true, "Payment method retrieved successfully", toDetailDto(method));
        });
    }

    @Override
    public GenericResponse<List<PaymentMethodSummaryDto>> getAllPaymentMethods() {
        return handle(() -> {
            List<PaymentMethod> methods = repository.findAll();
            List<PaymentMethodSummaryDto> dtoList = methods.stream()
                    .map(this::toSummaryDto)
                    .toList();
            return new GenericResponse<>(true, "Payment methods retrieved successfully", dtoList);
        });
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> create(CreateAndUpdatePaymentMethodRequest request) {
        return handle(() -> {
            PaymentMethod method = fromRequest(request);
            validate(method);
            PaymentMethod saved = repository.save(method);
            return new GenericResponse<>(true, "Payment method created successfully", toDetailDto(saved));
        });
    }

    @Override
    public GenericResponse<PaymentMethodDetailDto> update(UUID id, CreateAndUpdatePaymentMethodRequest request) {
        return handle(() -> {
            PaymentMethod existing = repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_MESSAGE));

            existing.setName(request.getName());
            existing.setType(request.getType());
            existing.setProvider(request.getProvider());
            existing.setAccountNumber(request.getAccountNumber());
            existing.setAccountName(request.getAccountName());

            validate(existing);
            PaymentMethod updated = repository.save(existing);
            return new GenericResponse<>(true, "Payment method updated successfully", toDetailDto(updated));
        });
    }

    @Override
    public GenericResponse<Void> delete(UUID id) {
        return handle(() -> {
            if (!repository.existsById(id)) {
                throw new NoSuchElementException(NOT_FOUND_MESSAGE);
            }
            repository.deleteById(id);
            return new GenericResponse<>(true, "Payment method deleted successfully", null);
        });
    }

    private void validate(PaymentMethod method) {
        if (method.getName() == null || method.getType() == null ||
                method.getProvider() == null || method.getAccountNumber() == null ||
                method.getAccountName() == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

        int length = method.getAccountNumber().length();
        PaymentType type = method.getType();

        if (type == PaymentType.BANK_TRANSFER && (length < 10 || length > 16)) {
            throw new IllegalArgumentException("Bank account number must be 10–16 digits");
        }

        if (type == PaymentType.E_WALLET && (length < 10 || length > 23)) {
            throw new IllegalArgumentException("Virtual account number must be 10–23 digits");
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

    /**
     * Common wrapper to handle exceptions and reduce repetitive try-catch blocks.
     */
    private <T> GenericResponse<T> handle(ServiceLogic<T> logic) {
        try {
            return logic.execute();
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return new GenericResponse<>(false, e.getMessage(), null);
        } catch (DataAccessException e) {
            return new GenericResponse<>(false, "Database error: " + e.getMessage(), null);
        }
    }

    @FunctionalInterface
    private interface ServiceLogic<T> {
        GenericResponse<T> execute();
    }
}
