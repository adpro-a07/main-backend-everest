package id.ac.ui.cs.advprog.everest.modules.repairorder.model;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@Table(name = "repair_orders")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
public class RepairOrder {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID customerId;

    @NotNull
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID technicianId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String itemName;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String itemCondition;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String issueDescription;

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate desiredServiceDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairOrderStatus status;

    // TODO: Add chosen payment method column
    // TODO: Add optional coupon column

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Protected constructor for JPA
    protected RepairOrder() {}
}