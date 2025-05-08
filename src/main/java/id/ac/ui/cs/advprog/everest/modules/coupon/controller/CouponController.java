package id.ac.ui.cs.advprog.everest.modules.coupon.controller;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable UUID id) {
        try {
            Coupon coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Coupon created = couponService.createCoupon(coupon);
        return ResponseEntity
                .created(URI.create("/api/v1/coupons/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable UUID id,
            @RequestBody Coupon coupon) {
        coupon.setId(id);
        Coupon updated = couponService.updateCoupon(coupon);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}