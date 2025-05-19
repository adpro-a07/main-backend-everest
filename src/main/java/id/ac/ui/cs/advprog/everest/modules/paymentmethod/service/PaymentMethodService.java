package id.ac.ui.cs.advprog.everest.modules.paymentmethod.service;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.CreateAndUpdatePaymentMethodRequest;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodDetailDto;
import id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto.PaymentMethodSummaryDto;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {
    GenericResponse<List<PaymentMethodSummaryDto>> getAllPaymentMethods();

    GenericResponse<PaymentMethodDetailDto> readDetails(UUID id);

    GenericResponse<PaymentMethodDetailDto> create(CreateAndUpdatePaymentMethodRequest request);

    GenericResponse<PaymentMethodDetailDto> update(UUID id, CreateAndUpdatePaymentMethodRequest request);

    GenericResponse<Void> delete(UUID id);
}
