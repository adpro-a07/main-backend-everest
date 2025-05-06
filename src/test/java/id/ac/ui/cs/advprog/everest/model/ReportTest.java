package id.ac.ui.cs.advprog.everest.model;

import java.time.LocalDate;

import id.ac.ui.cs.advprog.everest.model.enums.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {

    @Test
    public void testReportBuilder_Success() {
        Report laporan = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian motherboard dan pembersihan kipas")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();

        assertNotNull(laporan, "Report seharusnya tidak null setelah dibangun");
        assertEquals("Budi", laporan.getTechnicianName(), "Nama teknisi harus Budi");
        assertEquals("Penggantian motherboard dan pembersihan kipas", laporan.getRepairDetails(), "Detail pengerjaan tidak sesuai");
        assertEquals(LocalDate.now(), laporan.getRepairDate(), "Tanggal pengerjaan tidak sesuai");
        assertEquals(ReportStatus.COMPLETED, laporan.getStatus(), "Status laporan harus COMPLETED");
    }

    @Test
    public void testReportEquality() {
        Report laporan1 = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian hard drive")
                .repairDate(LocalDate.of(2025, 1, 15))
                .status(ReportStatus.COMPLETED)
                .build();

        Report laporan2 = Report.builder()
                .technicianName("Budi")
                .repairDetails("Penggantian hard drive")
                .repairDate(LocalDate.of(2025, 1, 15))
                .status(ReportStatus.COMPLETED)
                .build();

        assertEquals(laporan1, laporan2, "Report dengan atribut yang sama seharusnya sama");
        assertEquals(laporan1.hashCode(), laporan2.hashCode(), "Hash code harus sama karena laporan identik");
    }

    @Test
    void testReportBuilder_MissingStatus_ResultsInNullStatus() {
        Report laporan = Report.builder()
                .technicianName("Budi")
                .repairDetails("Perbaikan layar")
                .repairDate(LocalDate.now())
                .build();
        assertNull(laporan.getStatus(), "Status seharusnya null jika tidak di-set oleh builder");
    }
}
