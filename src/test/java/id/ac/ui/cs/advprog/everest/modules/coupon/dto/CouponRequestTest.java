package id.ac.ui.cs.advprog.everest.modules.coupon.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CouponRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidation() {
        CouponRequest request = CouponRequest.builder()
                .discountAmount(10000)
                .maxUsage(5)
                .validUntil(LocalDate.now().plusDays(30))
                .code("PROMO1234")
                .build();

        Set<ConstraintViolation<CouponRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailOnInvalidCodeFormat() {
        CouponRequest request = CouponRequest.builder()
                .discountAmount(10000)
                .maxUsage(5)
                .validUntil(LocalDate.now().plusDays(30))
                .code("invalid_code!") // Invalid
                .build();

        Set<ConstraintViolation<CouponRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Coupon code must be 6-12 alphanumeric characters in uppercase",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldFailOnPastDate() {
        CouponRequest request = CouponRequest.builder()
                .discountAmount(10000)
                .maxUsage(5)
                .validUntil(LocalDate.now().minusDays(1)) // Invalid
                .code("PROMO1234")
                .build();

        Set<ConstraintViolation<CouponRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Expiration date must be in the future",
                violations.iterator().next().getMessage()
        );
    }
}
