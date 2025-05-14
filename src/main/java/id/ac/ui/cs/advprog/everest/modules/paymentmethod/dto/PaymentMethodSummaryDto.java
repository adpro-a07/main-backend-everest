package id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodSummaryDto {
    private UUID id;
    private String name;
    private PaymentType type;
    private String provider;
}
