package id.ac.ui.cs.advprog.everest.modules.coupon.controller;

import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Test
    void testGetAllCoupons() throws Exception {
        Coupon coupon = Coupon.builder()
                .code("PROMO1212")
                .discountAmount(10000)
                .maxUsage(10)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        when(couponService.getAllCoupons()).thenReturn(List.of(coupon));

        mockMvc.perform(get("/admin/coupon"))
                .andExpect(status().isOk())
                .andExpect(view().name("coupon/list"))
                .andExpect(model().attributeExists("coupons"))
                .andExpect(model().attribute("coupons", List.of(coupon)));

        verify(couponService, times(1)).getAllCoupons();
    }

    @Test
    void testCreateCoupon() throws Exception {
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);

        mockMvc.perform(post("/admin/coupon/create")
                        .param("code", "")
                        .param("discountAmount", "15000")
                        .param("maxUsage", "5")
                        .param("validUntil", "2024-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/coupon"));

        verify(couponService, times(1)).createCoupon(couponCaptor.capture());

        Coupon capturedCoupon = couponCaptor.getValue();
        assertEquals(15000, capturedCoupon.getDiscountAmount());
        assertEquals(5, capturedCoupon.getMaxUsage());
        assertEquals(LocalDate.of(2024, 12, 31), capturedCoupon.getValidUntil());
    }

    @Test
    void testEditCouponForm() throws Exception {
        UUID couponId = UUID.randomUUID();
        Coupon coupon = Coupon.builder()
                .code("PROMO22")
                .discountAmount(20000)
                .maxUsage(3)
                .validUntil(LocalDate.now().plusDays(30))
                .build();

        when(couponService.getCouponById(couponId)).thenReturn(coupon);

        mockMvc.perform(get("/admin/coupon/edit/" + couponId))
                .andExpect(status().isOk())
                .andExpect(view().name("coupon/edit"))
                .andExpect(model().attributeExists("coupon"))
                .andExpect(model().attribute("coupon", coupon));

        verify(couponService, times(1)).getCouponById(couponId);
    }

    @Test
    void testUpdateCoupon() throws Exception {
        UUID couponId = UUID.randomUUID();
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);

        mockMvc.perform(post("/admin/coupon/update/" + couponId)
                        .param("code", "UPDATED123")
                        .param("discountAmount", "25000")
                        .param("maxUsage", "7")
                        .param("validUntil", "2025-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/coupon"));

        verify(couponService, times(1)).updateCoupon(couponCaptor.capture());

        Coupon capturedCoupon = couponCaptor.getValue();
        assertEquals(couponId, capturedCoupon.getId());
        assertEquals("UPDATED123", capturedCoupon.getCode());
        assertEquals(25000, capturedCoupon.getDiscountAmount());
        assertEquals(7, capturedCoupon.getMaxUsage());
        assertEquals(LocalDate.of(2025, 1, 1), capturedCoupon.getValidUntil());
    }

    @Test
    void testDeleteCoupon() throws Exception {
        UUID couponId = UUID.randomUUID();

        mockMvc.perform(get("/admin/coupon/delete/" + couponId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/coupon"));

        verify(couponService, times(1)).deleteCoupon(couponId);
    }
}