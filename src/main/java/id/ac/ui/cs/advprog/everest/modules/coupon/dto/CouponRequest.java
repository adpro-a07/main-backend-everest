package id.ac.ui.cs.advprog.everest.modules.coupon.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

    @NotNull(message = "Discount amount cannot be null")
    @Positive(message = "Discount amount must be positive")
    private Integer discountAmount;

    @NotNull(message = "Max usage cannot be null")
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;

    @NotNull(message = "Expiration date cannot be null")
    @Future(message = "Expiration date must be in the future")
    private LocalDate validUntil;

    @NotBlank(message = "Coupon code cannot be blank")
    @Pattern(
            regexp = "^[A-Z0-9]{6,12}$",
            message = "Coupon code must be 6-12 alphanumeric characters in uppercase"
    )
    private String code;

}