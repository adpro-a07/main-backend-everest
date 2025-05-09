package id.ac.ui.cs.advprog.everest.modules.coupon.repository;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CouponRepositoryTest {

    private CouponRepository couponRepository;
    private UUID couponId;
    private Coupon sampleCoupon;

    @BeforeEach
    void setUp() {
        couponRepository = new CouponRepository();

        // Create sample coupon for testing
        couponId = UUID.randomUUID();
        sampleCoupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(100)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();
        sampleCoupon.setId(couponId);
    }

    @Test
    void testSaveNewCoupon() {
        Coupon savedCoupon = couponRepository.save(sampleCoupon);

        assertNotNull(savedCoupon);
        assertNotNull(savedCoupon.getId());
        assertEquals(sampleCoupon.getCode(), savedCoupon.getCode());
        assertEquals(sampleCoupon.getDiscountAmount(), savedCoupon.getDiscountAmount());

        List<Coupon> allCoupons = couponRepository.findAll();
        assertEquals(1, allCoupons.size());
    }

    @Test
    void testFindById() {
        couponRepository.save(sampleCoupon);

        Optional<Coupon> foundCoupon = couponRepository.findById(couponId);

        assertTrue(foundCoupon.isPresent());
        assertEquals(sampleCoupon.getCode(), foundCoupon.get().getCode());
    }

    @Test
    void testFindByIdWithNonExistentId() {
        Optional<Coupon> foundCoupon = couponRepository.findById(UUID.randomUUID());

        assertFalse(foundCoupon.isPresent());
    }

    @Test
    void testUpdateExistingCoupon() {
        couponRepository.save(sampleCoupon);

        // Update the coupon
        sampleCoupon.setDiscountAmount(75000);
        sampleCoupon.setMaxUsage(200);

        Coupon updatedCoupon = couponRepository.save(sampleCoupon);

        assertEquals(75000, updatedCoupon.getDiscountAmount());
        assertEquals(200, updatedCoupon.getMaxUsage());

        // Verify only one coupon exists (update, not create new)
        List<Coupon> allCoupons = couponRepository.findAll();
        assertEquals(1, allCoupons.size());
    }

    @Test
    void testDeleteById() {
        couponRepository.save(sampleCoupon);

        couponRepository.deleteById(couponId);

        List<Coupon> allCoupons = couponRepository.findAll();
        assertEquals(0, allCoupons.size());

        Optional<Coupon> foundCoupon = couponRepository.findById(couponId);
        assertFalse(foundCoupon.isPresent());
    }

    @Test
    void testFindByValidUntilAfter() {
        // Save a valid coupon (expiry in future)
        couponRepository.save(sampleCoupon);

        // Save an expired coupon
        Coupon expiredCoupon = Coupon.builder()
                .code("EXPIRED")
                .discountAmount(20000)
                .maxUsage(50)
                .usageCount(0)
                .validUntil(LocalDate.now().minusDays(1))
                .build();
        expiredCoupon.setId(UUID.randomUUID());
        couponRepository.save(expiredCoupon);

        List<Coupon> validCoupons = couponRepository.findByValidUntilAfter(LocalDate.now());

        assertEquals(1, validCoupons.size());
        assertEquals("TEST50", validCoupons.get(0).getCode());
    }

    @Test
    void testFindByUsageCountLessThanMaxUsage() {
        // Save a coupon with available usages
        couponRepository.save(sampleCoupon);

        // Save a fully used coupon
        Coupon usedCoupon = Coupon.builder()
                .code("USED")
                .discountAmount(30000)
                .maxUsage(50)
                .usageCount(50)  // Max usage reached
                .validUntil(LocalDate.now().plusDays(30))
                .build();
        usedCoupon.setId(UUID.randomUUID());
        couponRepository.save(usedCoupon);

        List<Coupon> availableCoupons = couponRepository.findByUsageCountLessThanMaxUsage();

        assertEquals(1, availableCoupons.size());
        assertEquals("TEST50", availableCoupons.getFirst().getCode());
    }

    @Test
    void testExistsByCode() {
        couponRepository.save(sampleCoupon);

        assertTrue(couponRepository.existsByCode("TEST50"));
        assertFalse(couponRepository.existsByCode("NONEXISTENT"));
    }
}