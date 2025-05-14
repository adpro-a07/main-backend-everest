package id.ac.ui.cs.advprog.everest.modules.coupon.repository;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    private Coupon sampleCoupon;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        couponRepository.deleteAll();

        // Create sample coupon for testing
        sampleCoupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(100)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();
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
        // Save first to get the generated ID
        Coupon savedCoupon = couponRepository.save(sampleCoupon);
        UUID savedId = savedCoupon.getId();

        Optional<Coupon> foundCoupon = couponRepository.findById(savedId);

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
        // Save first to get the generated ID
        Coupon savedCoupon = couponRepository.save(sampleCoupon);

        // Update the coupon
        savedCoupon.setDiscountAmount(75000);
        savedCoupon.setMaxUsage(200);

        Coupon updatedCoupon = couponRepository.save(savedCoupon);

        assertEquals(75000, updatedCoupon.getDiscountAmount());
        assertEquals(200, updatedCoupon.getMaxUsage());

        // Verify only one coupon exists (update, not create new)
        List<Coupon> allCoupons = couponRepository.findAll();
        assertEquals(1, allCoupons.size());
    }

    @Test
    void testDeleteById() {
        // Save first to get the generated ID
        Coupon savedCoupon = couponRepository.save(sampleCoupon);
        UUID savedId = savedCoupon.getId();

        couponRepository.deleteById(savedId);

        List<Coupon> allCoupons = couponRepository.findAll();
        assertEquals(0, allCoupons.size());

        Optional<Coupon> foundCoupon = couponRepository.findById(savedId);
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
                .usageCount(50)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        couponRepository.save(usedCoupon);

        List<Coupon> availableCoupons = couponRepository.findByUsageCountLessThanMaxUsage();

        assertEquals(1, availableCoupons.size());
        assertEquals("TEST50", availableCoupons.get(0).getCode());
    }

    @Test
    void testExistsByCode() {
        couponRepository.save(sampleCoupon);

        assertTrue(couponRepository.existsByCode("TEST50"));
        assertFalse(couponRepository.existsByCode("NONEXISTENT"));
    }
}