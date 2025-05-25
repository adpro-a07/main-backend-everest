package id.ac.ui.cs.advprog.everest.modules.coupon.service;

import id.ac.ui.cs.advprog.everest.modules.coupon.dto.CouponRequest;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private UUID couponId;
    private CouponRequest validRequest;
    private CouponRequest invalidRequest;
    private Coupon savedEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        couponId = UUID.randomUUID();

        validRequest = CouponRequest.builder()
                .code("TEST50")
                .discountAmount(50000)
                .maxUsage(100)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        invalidRequest = CouponRequest.builder()
                .code("INV123")
                .discountAmount(-100)
                .maxUsage(0)
                .validUntil(LocalDate.now().minusDays(1))
                .build();

        // entity returned by repository after save
        savedEntity = Coupon.builder()
                .code(validRequest.getCode())
                .discountAmount(validRequest.getDiscountAmount())
                .maxUsage(validRequest.getMaxUsage())
                .usageCount(0)
                .validUntil(validRequest.getValidUntil())
                .build();
        savedEntity.setId(couponId);
    }

    @Test
    void testGetAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        list.add(savedEntity);
        when(couponRepository.findAll()).thenReturn(list);

        List<Coupon> result = couponService.getAllCoupons();

        assertEquals(1, result.size());
        assertEquals(savedEntity, result.get(0));
        verify(couponRepository).findAll();
    }

    @Test
    void testGetCouponById_Found() {
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(savedEntity));

        Coupon result = couponService.getCouponById(couponId);

        assertEquals(savedEntity, result);
        verify(couponRepository).findById(couponId);
    }

    @Test
    void testGetCouponById_NotFound() {
        UUID id2 = UUID.randomUUID();
        when(couponRepository.findById(id2)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> couponService.getCouponById(id2));
        verify(couponRepository).findById(id2);
    }

    @Test
    void testCreateCoupon_Success() {
        when(couponRepository.existsByCode(validRequest.getCode())).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedEntity);

        Coupon result = couponService.createCoupon(validRequest);

        assertNotNull(result);
        assertEquals(couponId, result.getId());
        assertEquals(validRequest.getCode(), result.getCode());
        assertEquals(0, result.getUsageCount());
        verify(couponRepository).existsByCode(validRequest.getCode());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testCreateCoupon_DuplicateCode() {
        when(couponRepository.existsByCode(validRequest.getCode())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> couponService.createCoupon(validRequest));
        verify(couponRepository).existsByCode(validRequest.getCode());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testCreateCoupon_InvalidData() {
        // invalidRequest has negative discount, zero maxUsage, past date
        when(couponRepository.existsByCode(invalidRequest.getCode())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> couponService.createCoupon(invalidRequest));
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testUpdateCoupon_Success() {
        Coupon existing = Coupon.builder()
                .code("OLD")
                .discountAmount(1000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(5))
                .build();
        existing.setId(couponId);

        CouponRequest upd = CouponRequest.builder()
                .code("NEW50")
                .discountAmount(2000)
                .maxUsage(20)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(existing));
        when(couponRepository.existsByCode(upd.getCode())).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        Coupon out = couponService.updateCoupon(couponId, upd);

        assertEquals("NEW50", out.getCode());
        assertEquals(2000, out.getDiscountAmount());
        verify(couponRepository).findById(couponId);
        verify(couponRepository).save(any());
    }

    @Test
    void testUpdateCoupon_DuplicateCode() {
        Coupon existing = savedEntity;
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(existing));
        when(couponRepository.existsByCode("DUP"))
                .thenReturn(true);
        CouponRequest dupReq = CouponRequest.builder()
                .code("DUP")
                .discountAmount(100)
                .maxUsage(1)
                .validUntil(LocalDate.now().plusDays(1))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> couponService.updateCoupon(couponId, dupReq));
    }

    @Test
    void testUpdateCoupon_NotFound() {
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> couponService.updateCoupon(couponId, validRequest));
    }

    @Test
    void testDeleteCoupon_Success() {
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(savedEntity));
        doNothing().when(couponRepository).deleteById(couponId);

        couponService.deleteCoupon(couponId);

        verify(couponRepository).deleteById(couponId);
    }

    @Test
    void testDeleteCoupon_NotFound() {
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> couponService.deleteCoupon(couponId));
    }

    @Test
    void testIsValidCoupon() {
        Coupon good = Coupon.builder()
                .code("C1")
                .discountAmount(10)
                .maxUsage(1)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        assertTrue(couponService.isValidCoupon(good));

        Coupon bad = Coupon.builder()
                .code("C2")
                .discountAmount(0)
                .maxUsage(0)
                .usageCount(0)
                .validUntil(LocalDate.now().minusDays(1))
                .build();
        assertFalse(couponService.isValidCoupon(bad));
    }
}
