package id.ac.ui.cs.advprog.everest.modules.coupon.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.coupon.dto.CouponRequest;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

class CouponControllerTest {

    private CouponService couponService;
    private CouponController controller;
    private AuthenticatedUser adminUser;

    @BeforeEach
    void setUp() {
        couponService = mock(CouponService.class);
        controller = new CouponController(couponService);
        adminUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "admin@example.com",
                "Admin User",
                UserRole.ADMIN,
                "555-1234",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );
    }

    @Test
    void testGetAllCoupons() {
        Coupon c1 = Coupon.builder()
                .code("X1")
                .discountAmount(100)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        c1.setId(UUID.randomUUID());
        when(couponService.getAllCoupons()).thenReturn(List.of(c1));

        ResponseEntity<List<Coupon>> resp = controller.getAllCoupons();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals(c1, resp.getBody().get(0));
        verify(couponService).getAllCoupons();
    }

    @Test
    void testGetById_Success() {
        UUID id = UUID.randomUUID();
        Coupon c = Coupon.builder()
                .code("Y2")
                .discountAmount(200)
                .maxUsage(3)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(5))
                .build();
        c.setId(id);
        when(couponService.getCouponById(id)).thenReturn(c);

        ResponseEntity<Coupon> resp = controller.getCouponById(id);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(c, resp.getBody());
    }

    @Test
    void testGetById_NotFound() {
        UUID id = UUID.randomUUID();
        when(couponService.getCouponById(id)).thenThrow(new RuntimeException("not found"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getCouponById(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testCreateCoupon_Success() {
        CouponRequest req = CouponRequest.builder()
                .code("NEW100")
                .discountAmount(1000)
                .maxUsage(10)
                .validUntil(LocalDate.now().plusDays(10))
                .build();
        Coupon created = Coupon.builder()
                .code(req.getCode())
                .discountAmount(req.getDiscountAmount())
                .maxUsage(req.getMaxUsage())
                .usageCount(0)
                .validUntil(req.getValidUntil())
                .build();
        created.setId(UUID.randomUUID());
        when(couponService.createCoupon(req)).thenReturn(created);

        ResponseEntity<Coupon> resp = controller.createCoupon(req, adminUser);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(created, resp.getBody());
        assertEquals(URI.create("/api/v1/coupons/" + created.getId()), resp.getHeaders().getLocation());
        verify(couponService).createCoupon(req);
    }

    @Test
    void testUpdateCoupon_Success() {
        UUID id = UUID.randomUUID();
        CouponRequest req = CouponRequest.builder()
                .code("UPD10")
                .discountAmount(500)
                .maxUsage(5)
                .validUntil(LocalDate.now().plusDays(5))
                .build();
        Coupon updated = Coupon.builder()
                .code(req.getCode())
                .discountAmount(req.getDiscountAmount())
                .maxUsage(req.getMaxUsage())
                .usageCount(0)
                .validUntil(req.getValidUntil())
                .build();
        updated.setId(id);
        when(couponService.updateCoupon(id, req)).thenReturn(updated);

        ResponseEntity<Coupon> resp = controller.updateCoupon(id, req, adminUser);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(updated, resp.getBody());
        verify(couponService).updateCoupon(id, req);
    }

    @Test
    void testDeleteCoupon() {
        UUID id = UUID.randomUUID();
        doNothing().when(couponService).deleteCoupon(id);

        ResponseEntity<Void> resp = controller.deleteCoupon(id, adminUser);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(couponService).deleteCoupon(id);
    }
}
