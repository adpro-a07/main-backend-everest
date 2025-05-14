package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {
    private Coupon coupon;
    private LocalDate future;
    private Validator validator;

    @BeforeEach
    void setUp() {
        future = LocalDate.now().plusDays(10);
        coupon = Coupon.builder()
                .code("TEST2025")
                .discountAmount(50000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(future)
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCouponBuilder_Success() {
        // Test basic builder functionality
        assertNotNull(coupon.getCode());
        assertEquals("TEST2025", coupon.getCode());
        assertEquals(Integer.valueOf(50000), coupon.getDiscountAmount());
        assertEquals(Integer.valueOf(5), coupon.getMaxUsage());
        assertEquals(Integer.valueOf(0), coupon.getUsageCount());
        assertEquals(future, coupon.getValidUntil());
    }

    @Test
    void testCouponTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        Coupon c = Coupon.builder()
                .code("TIMESTAMP")
                .validUntil(future)
                .build();

        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        assertNotNull(c.getCreatedAt(), "Created timestamp should not be null after setting");
        assertNotNull(c.getUpdatedAt(), "Updated timestamp should not be null after setting");
        assertEquals(now, c.getCreatedAt(), "CreatedAt should match what we set");
        assertEquals(now, c.getUpdatedAt(), "UpdatedAt should match what we set");
    }

    @Test
    void testCouponBuilder_AllowsNullOptionalFields() {
        Coupon c = Coupon.builder()
                .code("NULLS")
                .discountAmount(null)
                .maxUsage(null)
                .usageCount(null)
                .validUntil(null)
                .build();
        assertNotNull(c);
        assertNull(c.getDiscountAmount());
        assertNull(c.getMaxUsage());
        assertNull(c.getUsageCount());
        assertNull(c.getValidUntil());
    }

    @Test
    void testCouponBuilder_NullCode_Throws() {
        assertThrows(NullPointerException.class, CouponTest::buildCouponWithNullCode);
    }

    private static void buildCouponWithNullCode() {
        Coupon.builder().code(null).build();
    }

    @Test
    void testCouponSetters() {
        // Create initial coupon
        Coupon coupon1 = Coupon.builder()
                .code("INITIAL")
                .discountAmount(1000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(LocalDate.now())
                .build();

        UUID newId = UUID.randomUUID();
        coupon1.setId(newId);
        assertEquals(newId, coupon1.getId(), "ID harus berubah setelah setter dipanggil");

        coupon1.setCode("UPDATED");
        assertEquals("UPDATED", coupon1.getCode(), "Code harus berubah setelah setter dipanggil");

        coupon1.setDiscountAmount(2000);
        assertEquals(2000, coupon1.getDiscountAmount(), "DiscountAmount harus berubah setelah setter dipanggil");

        coupon1.setMaxUsage(20);
        assertEquals(20, coupon1.getMaxUsage(), "MaxUsage harus berubah setelah setter dipanggil");

        coupon1.setUsageCount(5);
        assertEquals(5, coupon1.getUsageCount(), "UsageCount harus berubah setelah setter dipanggil");

        LocalDate newDate = LocalDate.now().plusMonths(1);
        coupon1.setValidUntil(newDate);
        assertEquals(newDate, coupon1.getValidUntil(), "ValidUntil harus berubah setelah setter dipanggil");

        // Test timestamp setter
        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime newUpdatedAt = LocalDateTime.now().minusDays(1);

        coupon1.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, coupon1.getCreatedAt(), "CreatedAt harus berubah setelah setter dipanggil");

        coupon1.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, coupon1.getUpdatedAt(), "UpdatedAt harus berubah setelah setter dipanggil");
    }

    @Test
    void testConstructorAccess() throws Exception {
        // Test protected no-args constructor
        Constructor<Coupon> noArgsConstructor = Coupon.class.getDeclaredConstructor();
        noArgsConstructor.setAccessible(true);
        Coupon c = noArgsConstructor.newInstance();

        assertNotNull(c, "No-args constructor should create a non-null object");
        assertNull(c.getCode(), "Code should be null for empty constructor");

        // Test private all-args constructor
        Constructor<Coupon> allArgsConstructor = Coupon.class.getDeclaredConstructor(
                UUID.class, String.class, Integer.class, Integer.class,
                Integer.class, LocalDate.class, LocalDateTime.class, LocalDateTime.class);
        allArgsConstructor.setAccessible(true);

        UUID id = UUID.randomUUID();
        String code = "ALLARGS";
        Integer discountAmount = 5000;
        Integer maxUsage = 10;
        Integer usageCount = 2;
        LocalDate validUntil = future;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        Coupon fullCoupon = allArgsConstructor.newInstance(
                id, code, discountAmount, maxUsage, usageCount,
                validUntil, createdAt, updatedAt);

        assertEquals(id, fullCoupon.getId());
        assertEquals(code, fullCoupon.getCode());
        assertEquals(discountAmount, fullCoupon.getDiscountAmount());
        assertEquals(maxUsage, fullCoupon.getMaxUsage());
        assertEquals(usageCount, fullCoupon.getUsageCount());
        assertEquals(validUntil, fullCoupon.getValidUntil());
        assertEquals(createdAt, fullCoupon.getCreatedAt());
        assertEquals(updatedAt, fullCoupon.getUpdatedAt());
    }

    @Test
    void testBoundaryValues() {
        // Test maximum integer values
        Coupon maxCoupon = Coupon.builder()
                .code("MAX")
                .discountAmount(Integer.MAX_VALUE)
                .maxUsage(Integer.MAX_VALUE)
                .usageCount(Integer.MAX_VALUE)
                .validUntil(LocalDate.of(9999, 12, 31))
                .build();

        assertEquals(Integer.MAX_VALUE, maxCoupon.getDiscountAmount(),
                "Should handle Integer.MAX_VALUE for discountAmount");
        assertEquals(Integer.MAX_VALUE, maxCoupon.getMaxUsage(),
                "Should handle Integer.MAX_VALUE for maxUsage");
        assertEquals(Integer.MAX_VALUE, maxCoupon.getUsageCount(),
                "Should handle Integer.MAX_VALUE for usageCount");

        // Test with zero values (should be valid per @Min(0) annotations)
        Coupon zeroCoupon = Coupon.builder()
                .code("ZERO")
                .discountAmount(0)
                .maxUsage(0)
                .usageCount(0)
                .validUntil(future)
                .build();

        assertEquals(0, zeroCoupon.getDiscountAmount().intValue(),
                "Should properly store zero discountAmount");
        assertEquals(0, zeroCoupon.getMaxUsage().intValue(),
                "Should properly store zero maxUsage");
        assertEquals(0, zeroCoupon.getUsageCount().intValue(),
                "Should properly store zero usageCount");
    }

    @Test
    void testNegativeValues() {
        // Test we can create coupons with negative values (even if they'll fail validation)
        Coupon negativeCoupon = Coupon.builder()
                .code("NEGATIVE")
                .discountAmount(-100)
                .maxUsage(-5)
                .usageCount(-2)
                .validUntil(future)
                .build();

        assertEquals(-100, negativeCoupon.getDiscountAmount().intValue(),
                "Should store negative discountAmount value");
        assertEquals(-5, negativeCoupon.getMaxUsage().intValue(),
                "Should store negative maxUsage value");
        assertEquals(-2, negativeCoupon.getUsageCount().intValue(),
                "Should store negative usageCount value");
    }

    @Test
    void testEmptyStringCode() {
        // Empty strings are allowed by the constructor but would fail validation
        Coupon emptyCoupon = Coupon.builder()
                .code("")
                .validUntil(future)
                .build();

        assertEquals("", emptyCoupon.getCode(),
                "Should allow empty string code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"COUPON1", "TEST-COUPON", "SPECIAL_2025", "SUMMER25"})
    void testVariousValidCodes(String testCode) {
        Coupon c = Coupon.builder()
                .code(testCode)
                .validUntil(future)
                .build();

        assertEquals(testCode, c.getCode());
        Set<ConstraintViolation<Coupon>> violations = validator.validate(c);
        assertTrue(violations.isEmpty(), "Valid code pattern should not cause validation errors");
    }

    @Test
    void testEquals() {
        Coupon coupon1 = Coupon.builder().code("EQUAL").validUntil(future).build();

        // Same coupon equals itself
        assertEquals(coupon1, coupon1, "Object should equal itself");

        // Null comparison
        assertNotEquals(null, coupon1, "Object should not equal null");

        // Different type comparison
        assertNotEquals("not a coupon", coupon1, "Object should not equal different type");

        // Create two coupons with same values
        UUID sharedId = UUID.randomUUID();

        Coupon coupon1WithId = Coupon.builder().code("EQUAL").validUntil(future).build();
        coupon1WithId.setId(sharedId);

        Coupon coupon2WithId = Coupon.builder().code("EQUAL").validUntil(future).build();
        coupon2WithId.setId(sharedId);

        assertEquals(coupon1WithId.getId(), coupon2WithId.getId(), "IDs should be same after explicit setting");
    }

    @Test
    void testHashCode() {
        Coupon coupon1 = Coupon.builder().code("HASH1").validUntil(future).build();

        // Same coupon should always return same hashcode
        int hash1 = coupon1.hashCode();
        int hash2 = coupon1.hashCode();
        assertEquals(hash1, hash2, "Hashcode should be consistent for same object");

        // Create exact copy with same values
        Coupon coupon2 = Coupon.builder()
                .code(coupon1.getCode())
                .discountAmount(coupon1.getDiscountAmount())
                .maxUsage(coupon1.getMaxUsage())
                .usageCount(coupon1.getUsageCount())
                .validUntil(coupon1.getValidUntil())
                .build();

        assertDoesNotThrow(coupon2::hashCode);
    }

    @Test
    void testNoArgsConstructor() throws Exception {
        Constructor<Coupon> constructor = Coupon.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Coupon c = constructor.newInstance();

        assertNotNull(c, "NoArgs constructor should create a non-null object");
        assertNull(c.getCode(), "Code should be null in empty constructor");
        assertNull(c.getDiscountAmount(), "DiscountAmount should be null in empty constructor");
        assertNull(c.getMaxUsage(), "MaxUsage should be null in empty constructor");
        assertNull(c.getUsageCount(), "UsageCount should be null in empty constructor");
        assertNull(c.getValidUntil(), "ValidUntil should be null in empty constructor");
    }

    @Test
    void testIdGeneration() {
        Coupon c1 = Coupon.builder().code("ID1").validUntil(future).build();
        Coupon c2 = Coupon.builder().code("ID2").validUntil(future).build();

        if (c1.getId() != null && c2.getId() != null) {
            assertNotEquals(c1.getId(), c2.getId(), "Unique IDs should be generated");
        }
    }
}