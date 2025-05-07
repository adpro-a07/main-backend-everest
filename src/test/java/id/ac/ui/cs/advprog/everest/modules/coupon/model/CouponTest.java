package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CouponTest {

    @Test
    public void testCouponBuilder_Success() {
        Coupon coupon = Coupon.builder()
                .code("TEST2025")
                .discountAmount(50000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        // Verifikasi bahwa objek berhasil dibangun
        assertNotNull(coupon, "Coupon seharusnya tidak null setelah dibangun");
        assertNotNull(coupon.getId(), "ID harus ter-generate dan tidak boleh null");
        assertTrue(isValidUUID(coupon.getId()), "ID harus berupa UUID yang valid");

        assertEquals("TEST2025", coupon.getCode(), "Kode coupon harus TEST2025");
        assertEquals(50000, coupon.getDiscountAmount(), "Besaran diskon harus 5000");
        assertEquals(5, coupon.getMaxUsage(), "Jumlah penggunaan maksimal harus 5");
        assertEquals(0, coupon.getUsageCount(), "Jumlah pemakaian awal harus 0");
        assertEquals(LocalDate.now().plusDays(10), coupon.getValidUntil(), "Tanggal valid until tidak sesuai");
    }

    @Test
    public void testCouponBuilder_InvalidDiscount() {
        Coupon coupon = Coupon.builder()
                .code("INVALID2025")
                .discountAmount(-1000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(5))
                .build();

        assertTrue(coupon.getDiscountAmount() < 0,
                "Diskon negatif harus dianggap sebagai input yang tidak valid");
    }

    @Test
    public void testCoupon_NullCode_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            Coupon.builder()
                  .code(null)
                  .discountAmount(5000)
                  .maxUsage(5)
                  .usageCount(0)
                  .validUntil(LocalDate.now().plusDays(10))
                  .build();
        }, "Membangun Coupon dengan code null harus melempar NullPointerException");
    }

    @Test
    public void testCouponFieldValueEquality() {
        Coupon coupon1 = Coupon.builder()
                .code("EQUAL2025")
                .discountAmount(2500)
                .maxUsage(3)
                .usageCount(1)
                .validUntil(LocalDate.of(2025, 12, 31))
                .build();

        Coupon coupon2 = Coupon.builder()
                .code("EQUAL2025")
                .discountAmount(2500)
                .maxUsage(3)
                .usageCount(1)
                .validUntil(LocalDate.of(2025, 12, 31))
                .build();

        // Pastikan ID kedua coupon tidak null dan berbeda
        assertNotNull(coupon1.getId(), "ID coupon1 tidak boleh null");
        assertNotNull(coupon2.getId(), "ID coupon2 tidak boleh null");
        assertTrue(!coupon1.getId().equals(coupon2.getId()), "ID harus unik antara dua coupon");

        // Memeriksa bahwa field-field lain memiliki nilai yang sama
        assertEquals(coupon1.getCode(), coupon2.getCode(), "Kode harus sama");
        assertEquals(coupon1.getDiscountAmount(), coupon2.getDiscountAmount(), "Besaran diskon harus sama");
        assertEquals(coupon1.getMaxUsage(), coupon2.getMaxUsage(), "Max usage harus sama");
        assertEquals(coupon1.getUsageCount(), coupon2.getUsageCount(), "Usage count harus sama");
        assertEquals(coupon1.getValidUntil(), coupon2.getValidUntil(), "Valid until harus sama");
    }
    
    private boolean isValidUUID(UUID uuid) {
        try {
            UUID.fromString(uuid.toString());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
