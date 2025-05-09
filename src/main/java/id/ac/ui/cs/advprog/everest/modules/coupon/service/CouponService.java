package id.ac.ui.cs.advprog.everest.modules.coupon.service;

import id.ac.ui.cs.advprog.everest.modules.coupon.dto.CouponRequest;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import java.util.List;
import java.util.UUID;

public interface CouponService {
    List<Coupon> getAllCoupons();
    Coupon getCouponById(UUID id);
    Coupon createCoupon(CouponRequest couponRequest);
    Coupon updateCoupon(UUID id, CouponRequest couponRequest);
    void deleteCoupon(UUID id);
    boolean isValidCoupon(Coupon coupon);
}