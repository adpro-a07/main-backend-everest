package id.ac.ui.cs.advprog.everest.modules.coupon.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.modules.coupon.dto.CouponRequest;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable UUID id) {
        try {
            Coupon coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(
            @Valid @RequestBody CouponRequest couponRequest,
            @CurrentUser AuthenticatedUser user
    ) {

        Coupon created = couponService.createCoupon(couponRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/coupons/" + created.getId()))
                .body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable UUID id,
            @Valid @RequestBody CouponRequest couponRequest,
            @CurrentUser AuthenticatedUser user
    ) {
        Coupon updated = couponService.updateCoupon(id, couponRequest);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser user
    ) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
