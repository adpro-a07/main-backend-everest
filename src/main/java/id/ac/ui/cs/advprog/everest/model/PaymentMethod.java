package id.ac.ui.cs.advprog.everest.model;

import id.ac.ui.cs.advprog.everest.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentMethod {
    private UUID id;
    private String name;
    private PaymentType type;
    private String provider;
    private String accountNumber;
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
