package id.ac.ui.cs.advprog.everest.modules.paymentmethod.repository;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
}