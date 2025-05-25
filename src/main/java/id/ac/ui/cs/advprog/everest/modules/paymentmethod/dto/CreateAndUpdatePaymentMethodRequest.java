package id.ac.ui.cs.advprog.everest.modules.paymentmethod.dto;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;


@Getter
@Setter
public class CreateAndUpdatePaymentMethodRequest {

    @NotNull
    private PaymentType type;

    @NotBlank
    @Size(max = 100)
    private String provider;

    @NotBlank
    @Size(min = 10, max = 30)
    @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank
    @Size(max = 100)
    private String accountName;
}
