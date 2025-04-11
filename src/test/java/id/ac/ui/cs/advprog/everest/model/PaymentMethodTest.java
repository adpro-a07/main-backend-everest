package id.ac.ui.cs.advprog.everest.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PaymentMethodTest {

    @Test
    public void testBankTransferPaymentMethod() {
        PaymentMethod bankTransfer = new PaymentMethodBuilder()
                .withName("Transfer Bank")
                .withType(PaymentType.BANK_TRANSFER)
                .withProvider("Mandiri")
                .withAccountNumber("1122334455")
                .withAccountName("PT PerbaikiinAja")
                .build();

        assertEquals("Transfer Bank", bankTransfer.getName());
        assertEquals(PaymentType.BANK_TRANSFER, bankTransfer.getType());
        assertEquals("Mandiri", bankTransfer.getProvider());
        assertEquals("1122334455", bankTransfer.getAccountNumber());
        assertEquals("PT PerbaikiinAja", bankTransfer.getAccountName());
    }

    @Test
    public void testEWalletPaymentMethod() {
        PaymentMethod eWallet = new PaymentMethodBuilder()
                .withName("E-Wallet")
                .withType(PaymentType.E_WALLET)
                .withProvider("GoPay")
                .withAccountNumber("081298765432")
                .withAccountName("PerbaikiinAja")
                .build();

        assertEquals("E-Wallet", eWallet.getName());
        assertEquals(PaymentType.E_WALLET, eWallet.getType());
        assertEquals("GoPay", eWallet.getProvider());
        assertEquals("081298765432", eWallet.getAccountNumber());
        assertEquals("PerbaikiinAja", eWallet.getAccountName());
    }
}
