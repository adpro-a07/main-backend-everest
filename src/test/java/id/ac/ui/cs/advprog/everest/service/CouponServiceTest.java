package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.Coupon;
import id.ac.ui.cs.advprog.everest.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private UUID couponId;
    private Coupon validCoupon;
    private Coupon invalidCoupon;

    @BeforeEach
    void setUp() {
        couponId = UUID.randomUUID();

        // Set up a valid coupon
        validCoupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(100)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();
        validCoupon.setId(couponId);

        // Set up an invalid coupon (negative discount)
        invalidCoupon = Coupon.builder()
                .code("INVALID")
                .discountAmount(-1000)
                .maxUsage(0)
                .usageCount(0)
                .validUntil(LocalDate.now().minusDays(1))
                .build();
        invalidCoupon.setId(UUID.randomUUID());
    }

    @Test
    void testGetAllCoupons() {
        // Setup
        List<Coupon> couponList = new ArrayList<>();
        couponList.add(validCoupon);

        when(couponRepository.findAll()).thenReturn(couponList);

        // Execute
        List<Coupon> result = couponService.getAllCoupons();

        // Verify
        assertEquals(1, result.size());
        assertEquals(validCoupon.getCode(), result.get(0).getCode());
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testGetCouponById() {
        // Setup
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(validCoupon));

        // Execute
        Coupon result = couponService.getCouponById(couponId);

        // Verify
        assertNotNull(result);
        assertEquals(validCoupon.getCode(), result.getCode());
        verify(couponRepository, times(1)).findById(couponId);
    }

    @Test
    void testGetCouponByIdNotFound() {
        // Setup
        UUID nonExistentId = UUID.randomUUID();
        when(couponRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> {
            couponService.getCouponById(nonExistentId);
        });
        verify(couponRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testCreateCouponValid() {
        // Setup
        when(couponRepository.save(any(Coupon.class))).thenReturn(validCoupon);

        // Execute
        Coupon result = couponService.createCoupon(validCoupon);

        // Verify
        assertNotNull(result);
        assertEquals(validCoupon.getCode(), result.getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testCreateCouponInvalid() {
        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(invalidCoupon);
        });

        // The repository save method should not be called for invalid coupon
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void testCreateCouponWithCodeGeneration() {
        // Setup
        Coupon couponWithoutCode = Coupon.builder()
                .code("SUPER50")
                .discountAmount(50000)
                .maxUsage(100)
                .usageCount(null)
                .validUntil(LocalDate.now().plusDays(30))
                .build();


        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon savedCoupon = invocation.getArgument(0);
            savedCoupon.setId(UUID.randomUUID());
            return savedCoupon;
        });

        // Execute
        Coupon result = couponService.createCoupon(couponWithoutCode);

        // Verify
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertEquals(0, result.getUsageCount());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testUpdateCouponValid() {
        // Setup
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(validCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(validCoupon);

        // Execute
        Coupon result = couponService.updateCoupon(validCoupon);

        // Verify
        assertNotNull(result);
        assertEquals(validCoupon.getCode(), result.getCode());
        verify(couponRepository, times(1)).findById(couponId);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testUpdateCouponInvalid() {
        // Setup
        when(couponRepository.findById(invalidCoupon.getId())).thenReturn(Optional.of(invalidCoupon));

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.updateCoupon(invalidCoupon);
        });

        verify(couponRepository, times(1)).findById(invalidCoupon.getId());
        // The repository save method should not be called for invalid coupon
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void testUpdateCouponNotFound() {
        // Setup
        UUID nonExistentId = UUID.randomUUID();
        validCoupon.setId(nonExistentId);
        when(couponRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> {
            couponService.updateCoupon(validCoupon);
        });
        verify(couponRepository, times(1)).findById(nonExistentId);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void testDeleteCoupon() {
        // Setup
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(validCoupon));
        doNothing().when(couponRepository).deleteById(couponId);

        // Execute
        couponService.deleteCoupon(couponId);

        // Verify
        verify(couponRepository, times(1)).findById(couponId);
        verify(couponRepository, times(1)).deleteById(couponId);
    }

    @Test
    void testDeleteCouponNotFound() {
        // Setup
        UUID nonExistentId = UUID.randomUUID();
        when(couponRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> {
            couponService.deleteCoupon(nonExistentId);
        });
        verify(couponRepository, times(1)).findById(nonExistentId);
        verify(couponRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testIsValidCouponWithValidCoupon() {
        // Execute
        boolean result = couponService.isValidCoupon(validCoupon);

        // Verify
        assertTrue(result);
    }

    @Test
    void testIsValidCouponWithNegativeDiscount() {
        // Setup
        Coupon coupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(-1000)  // Negative discount
                .maxUsage(100)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        // Execute
        boolean result = couponService.isValidCoupon(coupon);

        // Verify
        assertFalse(result);
    }

    @Test
    void testIsValidCouponWithZeroMaxUsage() {
        // Setup
        Coupon coupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(0)  // Zero max usage
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        // Execute
        boolean result = couponService.isValidCoupon(coupon);

        // Verify
        assertFalse(result);
    }

    @Test
    void testIsValidCouponWithPastDate() {
        // Setup
        Coupon coupon = Coupon.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(100)
                .usageCount(0)
                .validUntil(LocalDate.now().minusDays(1))
                .build();

        // Execute
        boolean result = couponService.isValidCoupon(coupon);

        // Verify
        assertFalse(result);
    }
}