package id.ac.ui.cs.advprog.everest.modules.coupon.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

 class CouponTest {
    private Coupon coupon;
    private LocalDate future;

    @BeforeEach
    void init() {
        future = LocalDate.now().plusDays(10);
        coupon = Coupon.builder()
                .code("TEST2025")
                .discountAmount(50000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(future)
                .build();
    }

    @Test
    void testCouponBuilder_Success() {
        assertNotNull(coupon.getId(), "ID harus ter-generate dan tidak boleh null");
        assertEquals("TEST2025", coupon.getCode());
        assertEquals(Integer.valueOf(50000), coupon.getDiscountAmount());
        assertEquals(Integer.valueOf(5), coupon.getMaxUsage());
        assertEquals(Integer.valueOf(0), coupon.getUsageCount());
        assertEquals(future, coupon.getValidUntil());
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

    private Coupon buildCouponWithNullCode() {
        return Coupon.builder()
                .code(null)
                .build();
    }

    @Test
    void testCouponBuilder_NullCode_Throws() {
        assertThrows(NullPointerException.class, this::buildCouponWithNullCode);
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
    void testIdUniqueness() {
        Coupon a = Coupon.builder().code("A").validUntil(future).build();
        Coupon b = Coupon.builder().code("B").validUntil(future).build();
        assertNotEquals(a.getId(), b.getId(), "ID harus unik");
    }

    @Test
    void testCouponBuilder_InvalidDiscount() {
        Coupon coupon = Coupon.builder()
                .code("INVALID2025")
                .discountAmount(-1000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now().plusDays(5))
                .build();

        assertTrue(coupon.getDiscountAmount() < 0);
    }


    @Test
    void testCouponFieldValueEquality() {
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
    void testCouponSetters() {
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
    void testCouponWithNullValues() {
        Coupon coupon = Coupon.builder()
                .code("NULLTEST")
                .discountAmount(null)
                .maxUsage(null)
                .usageCount(null)
                .validUntil(null)
                .build();

        assertNotNull(coupon, "Coupon harus berhasil dibuat meski beberapa nilai null");
        assertEquals("NULLTEST", coupon.getCode());
        assertNull(coupon.getDiscountAmount());
        assertNull(coupon.getMaxUsage());
        assertNull(coupon.getUsageCount());
        assertNull(coupon.getValidUntil());
    }

    @Test
    void testCouponIdGeneration() {
        Coupon coupon1 = Coupon.builder()
                .code("AUTOID")
                .build();

        assertNotNull(coupon1.getId(), "ID harus otomatis digenerate");
        assertTrue(isValidUUID(coupon1.getId()), "ID harus UUID yang valid");

        UUID customId = UUID.randomUUID();
        Coupon coupon2 = Coupon.builder()
                .code("CUSTOMID")
                .discountAmount(1000)
                .maxUsage(5)
                .usageCount(0)
                .validUntil(LocalDate.now())
                .build();

        coupon2.setId(customId);

        assertEquals(customId, coupon2.getId(), "Custom ID harus tetap dipertahankan");
    }

    @Test
    void testBoundaryValues() {
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

    @Test
    void testProtectedConstructorAndSyncId() throws Exception {
        // Akses constructor protected
        Constructor<Coupon> constructor = Coupon.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Coupon coupon = constructor.newInstance();

        // Verifikasi ID null sebelum sync
        assertNull(coupon.getId());

        // Panggil method syncId via reflection
        Method syncIdMethod = Coupon.class.getDeclaredMethod("syncId");
        syncIdMethod.setAccessible(true);
        syncIdMethod.invoke(coupon);

        // Verifikasi ID terisi setelah sync
        assertNotNull(coupon.getId());
        assertEquals(coupon.getGeneratedId(), coupon.getId());
    }

    @Test
    void testSyncIdPreserveExistingId() throws Exception {
        Coupon coupon = Coupon.builder()
                .code("TEST")
                .build();

        UUID originalId = coupon.getId();

        // Panggil syncId
        Method syncIdMethod = Coupon.class.getDeclaredMethod("syncId");
        syncIdMethod.setAccessible(true);
        syncIdMethod.invoke(coupon);

        // Verifikasi ID tetap sama
        assertEquals(originalId, coupon.getId());
    }

    @Test
    void testGeneratedIdInitializedInProtectedConstructor() throws Exception {
        Constructor<Coupon> constructor = Coupon.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Coupon coupon = constructor.newInstance();

        assertNotNull(coupon.getGeneratedId());
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