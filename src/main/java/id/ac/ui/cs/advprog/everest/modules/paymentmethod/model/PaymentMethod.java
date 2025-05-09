package id.ac.ui.cs.advprog.everest.modules.paymentmethod.model;

import id.ac.ui.cs.advprog.everest.modules.paymentmethod.model.enums.PaymentType;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@Table(name = "payment_methods")
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentMethod {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String provider;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false)
    private String accountNumber;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String accountName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PaymentMethod() {
        // Required by JPA
    }

}
