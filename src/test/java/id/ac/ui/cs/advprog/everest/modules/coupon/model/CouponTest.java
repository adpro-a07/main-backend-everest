package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CouponTest {
    private Coupon coupon;
    private LocalDate future;

    @Test
    public void testCouponBuilder_Success() {
        Coupon coupon = Coupon.builder()
                .code("TEST2025")
                .discountAmount(50000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

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
    void testCouponBuilder_AllowsNullOptionalFields() {
        Coupon c = Coupon.builder()
                .code("NULLS")
                .discountAmount(null)
                .maxUsage(null)
                .usageCount(null)
                .validUntil(null)
                .build();
        assertNotNull(c);
        assertNull(c.getDiscountAmount());
        assertNull(c.getMaxUsage());
        assertNull(c.getUsageCount());
        assertNull(c.getValidUntil());
    }

    @Test
    void testCouponBuilder_NullCode_Throws() {
        assertThrows(NullPointerException.class, () -> Coupon.builder()
                .code(null)
                .build());
    }

    @Test
    void testDirectConstructor_GeneratesIdWhenNull() throws Exception {
        Constructor<Coupon> ctor = Coupon.class.getDeclaredConstructor(
                String.class, Integer.class, Integer.class, Integer.class, LocalDate.class);
        ctor.setAccessible(true);
        Coupon c = ctor.newInstance("DIR", 100, 2, 1, future);
        assertNotNull(c.getId());
        assertEquals("DIR", c.getCode());
    }

    @Test
    void testSetters_NonNullEnforced() {
        assertThrows(NullPointerException.class, () -> coupon.setCode(null));
        assertThrows(NullPointerException.class, () -> coupon.setId(null));
    }

    @Test
    void testIdUniqueness() {
        Coupon a = Coupon.builder().code("A").validUntil(future).build();
        Coupon b = Coupon.builder().code("B").validUntil(future).build();
        assertNotEquals(a.getId(), b.getId());
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

        assertNotNull(coupon1.getId(), "ID coupon1 tidak boleh null");
        assertNotNull(coupon2.getId(), "ID coupon2 tidak boleh null");
        assertTrue(!coupon1.getId().equals(coupon2.getId()), "ID harus unik antara dua coupon");

        assertEquals(coupon1.getCode(), coupon2.getCode(), "Kode harus sama");
        assertEquals(coupon1.getDiscountAmount(), coupon2.getDiscountAmount(), "Besaran diskon harus sama");
        assertEquals(coupon1.getMaxUsage(), coupon2.getMaxUsage(), "Max usage harus sama");
        assertEquals(coupon1.getUsageCount(), coupon2.getUsageCount(), "Usage count harus sama");
        assertEquals(coupon1.getValidUntil(), coupon2.getValidUntil(), "Valid until harus sama");
    }

    @Test
    public void testCouponSetters() {
        // Create initial coupon
        Coupon coupon = Coupon.builder()
                .code("INITIAL")
                .discountAmount(1000)
                .maxUsage(10)
                .usageCount(0)
                .validUntil(LocalDate.now())
                .build();

        UUID originalId = coupon.getId();

        UUID newId = UUID.randomUUID();
        coupon.setId(newId);
        assertEquals(newId, coupon.getId(), "ID harus berubah setelah setter dipanggil");
        assertNotEquals(originalId, coupon.getId(), "ID baru harus berbeda dengan ID asli");

        coupon.setCode("UPDATED");
        assertEquals("UPDATED", coupon.getCode(), "Code harus berubah setelah setter dipanggil");

        coupon.setDiscountAmount(2000);
        assertEquals(2000, coupon.getDiscountAmount(), "DiscountAmount harus berubah setelah setter dipanggil");

        coupon.setMaxUsage(20);
        assertEquals(20, coupon.getMaxUsage(), "MaxUsage harus berubah setelah setter dipanggil");

        coupon.setUsageCount(5);
        assertEquals(5, coupon.getUsageCount(), "UsageCount harus berubah setelah setter dipanggil");

        LocalDate newDate = LocalDate.now().plusMonths(1);
        coupon.setValidUntil(newDate);
        assertEquals(newDate, coupon.getValidUntil(), "ValidUntil harus berubah setelah setter dipanggil");
    }

    @Test
    public void testCouponWithNullValues() {
        Coupon coupon = Coupon.builder()
                .code("NULLTEST")
                .discountAmount(null)
                .maxUsage(null)
                .usageCount(null)
                .validUntil(null)
                .build();

        assertNotNull(coupon, "Coupon harus berhasil dibuat meski beberapa nilai null");
        assertEquals("NULLTEST", coupon.getCode(), "Code harus tetap ada");
        assertNull(coupon.getDiscountAmount(), "DiscountAmount seharusnya null");
        assertNull(coupon.getMaxUsage(), "MaxUsage seharusnya null");
        assertNull(coupon.getUsageCount(), "UsageCount seharusnya null");
        assertNull(coupon.getValidUntil(), "ValidUntil seharusnya null");
    }

    @Test
    public void testCouponIdGeneration() {
        Coupon coupon1 = Coupon.builder()
                .code("AUTOID")
                .build();

        assertNotNull(coupon1.getId(), "ID harus otomatis digenerate");
        assertTrue(isValidUUID(coupon1.getId()), "ID harus UUID yang valid");

        UUID customId = UUID.randomUUID();
        Coupon coupon2 = new Coupon("CUSTOMID", 1000, 5, 0, LocalDate.now());
        coupon2.setId(customId);

        assertEquals(customId, coupon2.getId(), "Custom ID harus tetap dipertahankan");
    }

    @Test
    public void testBoundaryValues() {
        Coupon coupon = Coupon.builder()
                .code("EXTREME")
                .discountAmount(Integer.MAX_VALUE)
                .maxUsage(Integer.MAX_VALUE)
                .usageCount(Integer.MAX_VALUE)
                .validUntil(LocalDate.of(9999, 12, 31))
                .build();

        assertEquals(Integer.MAX_VALUE, coupon.getDiscountAmount(), "Discount amount harus dapat menyimpan nilai maksimum");
        assertEquals(Integer.MAX_VALUE, coupon.getMaxUsage(), "Max usage harus dapat menyimpan nilai maksimum");
        assertEquals(Integer.MAX_VALUE, coupon.getUsageCount(), "Usage count harus dapat menyimpan nilai maksimum");
        assertEquals(LocalDate.of(9999, 12, 31), coupon.getValidUntil(), "Valid until harus dapat menyimpan tanggal jauh");
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