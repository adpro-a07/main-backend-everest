package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NotBlank
    @Column(nullable = false, unique = true)
    @NonNull
    private String code;

    @Min(0)
    @Column
    private Integer discountAmount;

    @Min(0)
    @Column
    private Integer maxUsage;

    @Min(0)
    @Column
    private Integer usageCount;

    @NotNull
    @Column
    private LocalDate validUntil;

    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Force new UUID if not provided by builder.
    @Builder.Default
    private UUID generatedId = UUID.randomUUID();

    protected Coupon() {
        this.generatedId = UUID.randomUUID();
    }

    @PostLoad
    @PostPersist
    private void syncId() {
        if (id == null) {
            id = generatedId;
        }
    }

    protected Coupon(String code, Integer discountAmount, Integer maxUsage,
                     Integer usageCount, LocalDate validUntil) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.discountAmount = discountAmount;
        this.maxUsage = maxUsage;
        this.usageCount = usageCount;
        this.validUntil = validUntil;
    }
}