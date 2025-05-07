package id.ac.ui.cs.advprog.everest.modules.coupon.service;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import java.util.List;
import java.util.UUID;

public interface CouponService {
    List<Coupon> getAllCoupons();
    Coupon getCouponById(UUID id);
    Coupon createCoupon(Coupon coupon);
    Coupon updateCoupon(Coupon coupon);
    void deleteCoupon(UUID id);
    boolean isValidCoupon(Coupon coupon);
}