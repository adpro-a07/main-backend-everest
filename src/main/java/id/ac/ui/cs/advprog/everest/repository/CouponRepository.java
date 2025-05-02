package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Coupon;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CouponRepository {
    private final List<Coupon> couponData = new ArrayList<>();

    public List<Coupon> findAll() {
        return new ArrayList<>(couponData);
    }

    public Optional<Coupon> findById(UUID id) {
        return couponData.stream()
                .filter(coupon -> coupon.getId().equals(id))
                .findFirst();
    }

    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            coupon.setId(UUID.randomUUID());
        }

        Optional<Coupon> existingCoupon = findById(coupon.getId());
        if (existingCoupon.isPresent()) {
            int index = couponData.indexOf(existingCoupon.get());
            couponData.set(index, coupon);
        } else {
            couponData.add(coupon);
        }

        return coupon;
    }

    public void deleteById(UUID id) {
        couponData.removeIf(coupon -> coupon.getId().equals(id));
    }

    public List<Coupon> findByValidUntilAfter(LocalDate date) {
        return couponData.stream()
                .filter(coupon -> coupon.getValidUntil() != null &&
                        coupon.getValidUntil().isAfter(date))
                .collect(Collectors.toList());
    }

    public List<Coupon> findByUsageCountLessThanMaxUsage() {
        return couponData.stream()
                .filter(coupon -> coupon.getUsageCount() != null &&
                        coupon.getMaxUsage() != null &&
                        coupon.getUsageCount() < coupon.getMaxUsage())
                .collect(Collectors.toList());
    }

    public boolean existsByCode(String code) {
        return couponData.stream()
                .anyMatch(coupon -> coupon.getCode().equals(code));
    }
}