package id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
    BANK_TRANSFER("Bank Transfer"),
    E_WALLET("E-Wallet");

    private final String value;

    private PaymentType(String value) {
        this.value = value;
    }

    public static boolean contains(String param) {
        for (PaymentType type : PaymentType.values()) {
            if (type.name().equals(param)) {
                return true;
            }
        }
        return false;
    }
}