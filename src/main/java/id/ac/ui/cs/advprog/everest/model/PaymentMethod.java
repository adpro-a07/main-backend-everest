package id.ac.ui.cs.advprog.everest.model;

import id.ac.ui.cs.advprog.everest.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PaymentMethod {
    private String name;
    private PaymentType type;
    private String provider;
    private String accountNumber;
    private String accountName;

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
        paymentMethod.setName(name);
        paymentMethod.setType(type);
        paymentMethod.setProvider(provider);
        paymentMethod.setAccountNumber(accountNumber);
        paymentMethod.setAccountName(accountName);
        return paymentMethod;
    }
}
