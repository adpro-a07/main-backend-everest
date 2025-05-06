package id.ac.ui.cs.advprog.everest.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ReportTest {

    @Test
    public void testReportBuilder_Success() {
        Report laporan = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian motherboard dan pembersihan kipas")
                .repairDate(LocalDate.now())
                .status("COMPLETED")
                .build();

        assertNotNull(laporan, "Report seharusnya tidak null setelah dibangun");
        assertEquals("Budi", laporan.getTechnicianName(), "Nama teknisi harus Budi");
        assertEquals("Penggantian motherboard dan pembersihan kipas", laporan.getRepairDetails(), "Detail pengerjaan tidak sesuai");
        assertEquals(LocalDate.now(), laporan.getRepairDate(), "Tanggal pengerjaan tidak sesuai");
        assertEquals("COMPLETED", laporan.getStatus(), "Status laporan harus COMPLETED");
    }

    @Test
    public void testReportEquality() {
        Report laporan1 = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian hard drive")
                .repairDate(LocalDate.of(2025, 1, 15))
                .status("PENDING")
                .build();

        Report laporan2 = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian hard drive")
                .repairDate(LocalDate.of(2025, 1, 15))
                .status("PENDING")
                .build();

        assertEquals(laporan1, laporan2, "Report dengan atribut yang sama seharusnya sama");
        assertEquals(laporan1.hashCode(), laporan2.hashCode(), "Hash code harus sama karena laporan identik");
    }
    
    @Test
    public void testReport_InvalidStatus() {
        Report laporan = Report.builder()
                .technicianName("Budi")
                .repairDetails("Perbaikan layar")
                .repairDate(LocalDate.now())
                .status("")  // Status kosong
                .build();
        assertTrue(laporan.getStatus().isEmpty(), "Status seharusnya kosong dan dianggap tidak valid oleh logika bisnis");
    }
}
