package id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PaymentMethodDetailDto {
    private UUID id;
    private String name;
    private PaymentType type;
    private String provider;
    private String accountNumber;
    private String accountName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
