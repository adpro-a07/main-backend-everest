package id.ac.ui.cs.advprog.everest.modules.coupon.service;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Autowired
    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponById(UUID id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        if (!isValidCoupon(coupon)) {
            throw new IllegalArgumentException("Invalid coupon data");
        }

        if (coupon.getCode().isEmpty()) {
            coupon.setCode(generateCouponCode());
        }

        if (coupon.getUsageCount() == null) {
            coupon.setUsageCount(0);
        }

        return couponRepository.save(coupon);
    }

    @Override
    public Coupon updateCoupon(Coupon coupon) {
        getCouponById(coupon.getId());

        if (!isValidCoupon(coupon)) {
            throw new IllegalArgumentException("Invalid coupon data");
        }

        return couponRepository.save(coupon);
    }

    @Override
    public void deleteCoupon(UUID id) {
        getCouponById(id);
        couponRepository.deleteById(id);
    }

    @Override
    public boolean isValidCoupon(Coupon coupon) {
        if (coupon.getDiscountAmount() == null || coupon.getDiscountAmount() <= 0) {
            return false;
        }

        if (coupon.getMaxUsage() == null || coupon.getMaxUsage() <= 0) {
            return false;
        }

        if (coupon.getValidUntil() == null || coupon.getValidUntil().isBefore(LocalDate.now())) {
            return false;
        }
        return true;
    }

    private String generateCouponCode() {
        String uuid = UUID.randomUUID().toString().toUpperCase();
        return "PROMO" + uuid.substring(0, 8);
    }
}