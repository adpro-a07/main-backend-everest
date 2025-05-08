package id.ac.ui.cs.advprog.everest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.coupon.controller.CouponController;
import id.ac.ui.cs.advprog.everest.modules.coupon.model.Coupon;
import id.ac.ui.cs.advprog.everest.modules.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthServiceGrpcClient authServiceGrpcClient;

    @Test
    void testGetAllCoupons() throws Exception {
        Coupon c = Coupon.builder()
                .code("PROMO1212")
                .discountAmount(10000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(30))
                .build();
        c.setId(UUID.randomUUID());

        when(couponService.getAllCoupons()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/v1/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(c.getId().toString())))
                .andExpect(jsonPath("$[0].code", is("PROMO1212")));

        verify(couponService).getAllCoupons();
    }

    @Test
    void testCreateCoupon() throws Exception {
        Coupon input = Coupon.builder()
                .code("XMAS")
                .discountAmount(15000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.of(2024,12,31))
                .build();

        Coupon saved = Coupon.builder()
                .code("XMAS")
                .discountAmount(15000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.of(2024,12,31))
                .build();
        saved.setId(UUID.randomUUID());

        when(couponService.createCoupon(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/coupons/" + saved.getId())))
                .andExpect(jsonPath("$.id", is(saved.getId().toString())))
                .andExpect(jsonPath("$.code", is("XMAS")));

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponService).createCoupon(captor.capture());
        assertEquals(15000, captor.getValue().getDiscountAmount());
    }

    @Test
    void testGetById_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(couponService.getCouponById(id)).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/v1/coupons/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCoupon() throws Exception {
        UUID id = UUID.randomUUID();
        Coupon input = Coupon.builder()
                .code("UPDATE")
                .discountAmount(20000)
                .maxUsage(3)
                .usageCount(0)
                .validUntil(LocalDate.of(2025,1,1))
                .build();
        Coupon updated = Coupon.builder()
                .code("UPDATE")
                .discountAmount(20000)
                .maxUsage(3)
                .usageCount(0)
                .validUntil(LocalDate.of(2025,1,1))
                .build();
        updated.setId(id);

        when(couponService.updateCoupon(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/coupons/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.code", is("UPDATE")));

        verify(couponService).updateCoupon(any(Coupon.class));
    }

    @Test
    void testDeleteCoupon() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(couponService).deleteCoupon(id);

        mockMvc.perform(delete("/api/v1/coupons/" + id))
                .andExpect(status().isNoContent());

        verify(couponService).deleteCoupon(id);
    }
}
