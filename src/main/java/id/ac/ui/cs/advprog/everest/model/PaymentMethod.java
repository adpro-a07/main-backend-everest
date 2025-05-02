package id.ac.ui.cs.advprog.everest.model;

import id.ac.ui.cs.advprog.everest.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull(message = "Name must not be null")
    private String name;

    @NotNull(message = "Type must not be null")
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @NotNull(message = "Provider must not be null")
    private String provider;

    @NotNull(message = "Account number must not be null")
    private String accountNumber;

    @NotNull(message = "Account name must not be null")
    private String accountName;

    public PaymentMethod withId(UUID id) {
        this.id = id;
        return this;
    }

    public PaymentMethod withName(String name) {
        this.name = name;
        return this;
    }

    public PaymentMethod withType(PaymentType type) {
        this.type = type;
        return this;
    }

    public PaymentMethod withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public PaymentMethod withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public PaymentMethod withAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public PaymentMethod build() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id != null ? id : UUID.randomUUID());
        paymentMethod.setName(name);
        paymentMethod.setType(type);
        paymentMethod.setProvider(provider);
        paymentMethod.setAccountNumber(accountNumber);
        paymentMethod.setAccountName(accountName);
        return paymentMethod;
    }
}
