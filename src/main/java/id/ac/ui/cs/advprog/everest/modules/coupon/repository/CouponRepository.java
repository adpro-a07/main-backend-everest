package id.ac.ui.cs.advprog.everest.modules.coupon.repository;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    List<Coupon> findByValidUntilAfter(LocalDate date);

    @Query("SELECT c FROM Coupon c WHERE c.usageCount < c.maxUsage")
    List<Coupon> findByUsageCountLessThanMaxUsage();

    boolean existsByCode(String code);
}
