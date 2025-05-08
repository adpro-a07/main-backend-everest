package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class Coupon {

    @NonNull
    private UUID id;

    @NonNull
    private String code;

    private Integer discountAmount;
    private Integer maxUsage;
    private Integer usageCount;
    private LocalDate validUntil;

    @Builder
    public Coupon(@NonNull String code, Integer discountAmount, Integer maxUsage, Integer usageCount, LocalDate validUntil) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.discountAmount = discountAmount;
        this.maxUsage = maxUsage;
        this.usageCount = usageCount;
        this.validUntil = validUntil;
    }
}
