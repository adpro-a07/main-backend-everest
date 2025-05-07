package id.ac.ui.cs.advprog.everest.modules.coupon.controller;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/coupon")
public class CouponController {

    private final CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("")
    public String getAllCoupons(Model model) {
        List<Coupon> coupons = couponService.getAllCoupons();
        model.addAttribute("coupons", coupons);
        return "coupon/list";
    }

    @PostMapping("/create")
    public String createCoupon(@ModelAttribute Coupon coupon) {
        couponService.createCoupon(coupon);
        return "redirect:/admin/coupon";
    }

    @GetMapping("/edit/{id}")
    public String editCouponForm(@PathVariable UUID id, Model model) {
        Coupon coupon = couponService.getCouponById(id);
        model.addAttribute("coupon", coupon);
        return "coupon/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCoupon(@PathVariable UUID id, @ModelAttribute Coupon coupon) {
        coupon.setId(id);
        couponService.updateCoupon(coupon);
        return "redirect:/admin/coupon";
    }

    @GetMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return "redirect:/admin/coupon";
    }
}
