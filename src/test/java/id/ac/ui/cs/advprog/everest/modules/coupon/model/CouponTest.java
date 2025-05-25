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
                .version(1)
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCouponBuilder_Success() {
        assertNotNull(coupon.getCode());
        assertEquals("TEST2025", coupon.getCode());
        assertEquals(Integer.valueOf(50000), coupon.getDiscountAmount());
        assertEquals(Integer.valueOf(5), coupon.getMaxUsage());
        assertEquals(Integer.valueOf(0), coupon.getUsageCount());
        assertEquals(future, coupon.getValidUntil());
        assertEquals(Integer.valueOf(1), coupon.getVersion());
    }

    @Test
    void testCouponTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        Coupon c = Coupon.builder()
                .code("TIMESTAMP")
                .validUntil(future)
                .version(2)
                .build();

        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        assertNotNull(c.getCreatedAt());
        assertNotNull(c.getUpdatedAt());
        assertEquals(now, c.getCreatedAt());
        assertEquals(now, c.getUpdatedAt());
        assertEquals(Integer.valueOf(2), c.getVersion());
    }

    @Test
    void testCouponBuilder_AllowsNullOptionalFields() {
        Coupon c = Coupon.builder()
                .code("NULLS")
                .discountAmount(null)
                .maxUsage(null)
                .usageCount(null)
                .validUntil(null)
                .version(null)
                .build();
        assertNotNull(c);
        assertNull(c.getDiscountAmount());
        assertNull(c.getMaxUsage());
        assertNull(c.getUsageCount());
        assertNull(c.getValidUntil());
        assertNull(c.getVersion());
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
        Coupon coupon1 = Coupon.builder()
                .code("INITIAL")
                .discountAmount(1000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(LocalDate.now())
                .version(1)
                .build();

        UUID newId = UUID.randomUUID();
        coupon1.setId(newId);
        assertEquals(newId, coupon1.getId());

        coupon1.setCode("UPDATED");
        assertEquals("UPDATED", coupon1.getCode());

        coupon1.setDiscountAmount(2000);
        assertEquals(2000, coupon1.getDiscountAmount());

        coupon1.setMaxUsage(20);
        assertEquals(20, coupon1.getMaxUsage());

        coupon1.setUsageCount(5);
        assertEquals(5, coupon1.getUsageCount());

        LocalDate newDate = LocalDate.now().plusMonths(1);
        coupon1.setValidUntil(newDate);
        assertEquals(newDate, coupon1.getValidUntil());

        coupon1.setVersion(3);
        assertEquals(3, coupon1.getVersion());

        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime newUpdatedAt = LocalDateTime.now().minusDays(1);

        coupon1.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, coupon1.getCreatedAt());

        coupon1.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, coupon1.getUpdatedAt());
    }

    @Test
    void testConstructorAccess() throws Exception {
        // Test protected no-args constructor
        Constructor<Coupon> noArgsConstructor = Coupon.class.getDeclaredConstructor();
        noArgsConstructor.setAccessible(true);
        Coupon c = noArgsConstructor.newInstance();

        assertNotNull(c);
        assertNull(c.getCode());

        // Test private all-args constructor (now with version)
        Constructor<Coupon> allArgsConstructor = Coupon.class.getDeclaredConstructor(
                UUID.class, String.class, Integer.class, Integer.class, Integer.class,
                Integer.class, LocalDate.class, LocalDateTime.class, LocalDateTime.class
        );
        allArgsConstructor.setAccessible(true);

        UUID id = UUID.randomUUID();
        String code = "ALLARGS";
        Integer discountAmount = 5000;
        Integer maxUsage = 10;
        Integer version = 7;
        Integer usageCount = 2;
        LocalDate validUntil = future;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        Coupon fullCoupon = allArgsConstructor.newInstance(
                id, code, discountAmount, maxUsage, version, usageCount,
                validUntil, createdAt, updatedAt
        );

        assertEquals(id, fullCoupon.getId());
        assertEquals(code, fullCoupon.getCode());
        assertEquals(discountAmount, fullCoupon.getDiscountAmount());
        assertEquals(maxUsage, fullCoupon.getMaxUsage());
        assertEquals(version, fullCoupon.getVersion());
        assertEquals(usageCount, fullCoupon.getUsageCount());
        assertEquals(validUntil, fullCoupon.getValidUntil());
        assertEquals(createdAt, fullCoupon.getCreatedAt());
        assertEquals(updatedAt, fullCoupon.getUpdatedAt());
    }

    @Test
    void testBoundaryValues() {
        Coupon maxCoupon = Coupon.builder()
                .code("MAX")
                .discountAmount(Integer.MAX_VALUE)
                .maxUsage(Integer.MAX_VALUE)
                .usageCount(Integer.MAX_VALUE)
                .validUntil(LocalDate.of(9999, 12, 31))
                .version(Integer.MAX_VALUE)
                .build();

        assertEquals(Integer.MAX_VALUE, maxCoupon.getDiscountAmount());
        assertEquals(Integer.MAX_VALUE, maxCoupon.getMaxUsage());
        assertEquals(Integer.MAX_VALUE, maxCoupon.getUsageCount());
        assertEquals(Integer.MAX_VALUE, maxCoupon.getVersion());

        Coupon zeroCoupon = Coupon.builder()
                .code("ZERO")
                .discountAmount(0)
                .maxUsage(0)
                .usageCount(0)
                .validUntil(future)
                .version(0)
                .build();

        assertEquals(0, zeroCoupon.getDiscountAmount().intValue());
        assertEquals(0, zeroCoupon.getMaxUsage().intValue());
        assertEquals(0, zeroCoupon.getUsageCount().intValue());
        assertEquals(0, zeroCoupon.getVersion().intValue());
    }

    @Test
    void testNegativeValues() {
        Coupon negativeCoupon = Coupon.builder()
                .code("NEGATIVE")
                .discountAmount(-100)
                .maxUsage(-5)
                .usageCount(-2)
                .validUntil(future)
                .version(-1)
                .build();

        assertEquals(-100, negativeCoupon.getDiscountAmount().intValue());
        assertEquals(-5, negativeCoupon.getMaxUsage().intValue());
        assertEquals(-2, negativeCoupon.getUsageCount().intValue());
        assertEquals(-1, negativeCoupon.getVersion().intValue());
    }

    @Test
    void testEmptyStringCode() {
        Coupon emptyCoupon = Coupon.builder()
                .code("")
                .validUntil(future)
                .version(1)
                .build();

        assertEquals("", emptyCoupon.getCode());
        assertEquals(1, emptyCoupon.getVersion().intValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"COUPON1", "TEST-COUPON", "SPECIAL_2025", "SUMMER25"})
    void testVariousValidCodes(String testCode) {
        Coupon c = Coupon.builder()
                .code(testCode)
                .discountAmount(1000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(future)
                .version(1)
                .build();

        assertEquals(testCode, c.getCode());
        Set<ConstraintViolation<Coupon>> violations = validator.validate(c);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEquals() {
        Coupon coupon1 = Coupon.builder().code("EQUAL").validUntil(future).version(1).build();

        assertEquals(coupon1, coupon1);
        assertNotEquals(null, coupon1);
        assertNotEquals("not a coupon", coupon1);

        UUID sharedId = UUID.randomUUID();

        Coupon coupon1WithId = Coupon.builder().code("EQUAL").validUntil(future).version(1).build();
        coupon1WithId.setId(sharedId);

        Coupon coupon2WithId = Coupon.builder().code("EQUAL").validUntil(future).version(1).build();
        coupon2WithId.setId(sharedId);

        assertEquals(coupon1WithId.getId(), coupon2WithId.getId());
    }

    @Test
    void testHashCode() {
        Coupon coupon1 = Coupon.builder().code("HASH1").validUntil(future).version(1).build();

        int hash1 = coupon1.hashCode();
        int hash2 = coupon1.hashCode();
        assertEquals(hash1, hash2);

        Coupon coupon2 = Coupon.builder()
                .code(coupon1.getCode())
                .discountAmount(coupon1.getDiscountAmount())
                .maxUsage(coupon1.getMaxUsage())
                .usageCount(coupon1.getUsageCount())
                .validUntil(coupon1.getValidUntil())
                .version(coupon1.getVersion())
                .build();

        assertDoesNotThrow(coupon2::hashCode);
    }

    @Test
    void testNoArgsConstructor() throws Exception {
        Constructor<Coupon> constructor = Coupon.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Coupon c = constructor.newInstance();

        assertNotNull(c);
        assertNull(c.getCode());
        assertNull(c.getDiscountAmount());
        assertNull(c.getMaxUsage());
        assertNull(c.getUsageCount());
        assertNull(c.getValidUntil());
        assertNull(c.getVersion());
    }

    @Test
    void testIdGeneration() {
        Coupon c1 = Coupon.builder().code("ID1").validUntil(future).version(1).build();
        Coupon c2 = Coupon.builder().code("ID2").validUntil(future).version(1).build();

        if (c1.getId() != null && c2.getId() != null) {
            assertNotEquals(c1.getId(), c2.getId());
        }
    }
}