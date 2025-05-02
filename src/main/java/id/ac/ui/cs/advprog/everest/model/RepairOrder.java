package id.ac.ui.cs.advprog.everest.model;

import id.ac.ui.cs.advprog.everest.model.enums.RepairOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "repair_orders")
@Getter
@Setter
public class RepairOrder {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, columnDefinition = "UUID")
    private UUID customerId;

    // Optional until assigned
    @Column(columnDefinition = "UUID")
    private UUID technicianId;

    @NotBlank
    private String itemName;

    @NotBlank
    private String itemCondition;

    @NotBlank
    private String issueDescription;

    @Column(nullable = false)
    private LocalDate desiredServiceDate;

    private boolean useCoupon;

    @Enumerated(EnumType.STRING)
    private RepairOrderStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

