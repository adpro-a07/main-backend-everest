package id.ac.ui.cs.advprog.everest.modules.coupon.service;

import id.ac.ui.cs.advprog.everest.modules.coupon.dto.CouponRequest;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Coupon createCoupon(CouponRequest couponRequest) {
        // Convert DTO ke Entity
        Coupon coupon = convertToEntity(couponRequest);


        // Validasi unik code
        if (!isValidCoupon(coupon)) {
            throw new IllegalArgumentException("Invalid coupon data");
        }
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        // Set nilai default
        coupon.setUsageCount(0);
        coupon.setCreatedAt(LocalDateTime.now());

        return couponRepository.save(coupon);
    }

    @Override
    public Coupon updateCoupon(UUID id, CouponRequest req) {
        Coupon existing = getCouponById(id);

        String oldCode = existing.getCode();
        String newCode = req.getCode();

        // Code‐uniqueness check happens against the NEW code—but only if it actually changed
        if (!oldCode.equals(newCode) && couponRepository.existsByCode(newCode)) {
            throw new IllegalArgumentException("New coupon code already exists");
        }

        // apply all updates
        existing.setCode(newCode);
        existing.setDiscountAmount(req.getDiscountAmount());
        existing.setMaxUsage(req.getMaxUsage());
        existing.setValidUntil(req.getValidUntil());
        existing.setUpdatedAt(LocalDateTime.now());

        if (!isValidCoupon(existing)) {
            throw new IllegalArgumentException("Invalid coupon data");
        }

        return couponRepository.save(existing);
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

        return coupon.getValidUntil() != null && !coupon.getValidUntil().isBefore(LocalDate.now());
    }

    private Coupon convertToEntity(CouponRequest request) {
        return Coupon.builder()
                .code(request.getCode())
                .discountAmount(request.getDiscountAmount())
                .maxUsage(request.getMaxUsage())
                .validUntil(request.getValidUntil())
                .build();
    }
}