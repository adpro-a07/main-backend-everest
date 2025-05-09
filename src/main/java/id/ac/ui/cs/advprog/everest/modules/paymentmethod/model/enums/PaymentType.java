package id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums;

import lombok.Getter;

@Getter

public enum PaymentType {
    BANK_TRANSFER("Bank Transfer"),
    E_WALLET("E-Wallet");

    private final String value;

    PaymentType(String value) {
        this.value = value;
    }
}

