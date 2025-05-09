package id.ac.ui.cs.advprog.everest.modules.report.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {

    private UUID testId;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCreatedAt = LocalDateTime.now().minusDays(1);
        testUpdatedAt = LocalDateTime.now();
    }

    @Test
    public void testReportBuilder_AllFields() {
        // Given
        LocalDate repairDate = LocalDate.now();

        // When
        Report laporan = Report.builder()
                .id(testId)
                .technicianName("Budi")
                .repairDetails("Penggantian motherboard dan pembersihan kipas")
                .repairDate(repairDate)
                .status(ReportStatus.COMPLETED)
                .createdAt(testCreatedAt)
                .updatedAt(testUpdatedAt)
                .build();

        // Then
        assertNotNull(laporan);
        assertEquals(testId, laporan.getId());
        assertEquals("Budi", laporan.getTechnicianName());
        assertEquals("Penggantian motherboard dan pembersihan kipas", laporan.getRepairDetails());
        assertEquals(repairDate, laporan.getRepairDate());
        assertEquals(ReportStatus.COMPLETED, laporan.getStatus());
        assertEquals(testCreatedAt, laporan.getCreatedAt());
        assertEquals(testUpdatedAt, laporan.getUpdatedAt());
    }

    @Test
    public void testReportNoArgsConstructor() {
        // When using protected constructor through reflection
        Report laporan = new Report();

        // Then
        assertNotNull(laporan);
        assertNull(laporan.getId());
        assertNull(laporan.getTechnicianName());
        assertNull(laporan.getRepairDetails());
        assertNull(laporan.getRepairDate());
        assertNull(laporan.getStatus());
        assertNull(laporan.getCreatedAt());
        assertNull(laporan.getUpdatedAt());
    }

    @Test
    public void testReportAllArgsConstructor() {
        // Given
        LocalDate repairDate = LocalDate.now();

        // When
        Report laporan = new Report(
                testId,
                "Budi",
                "Penggantian motherboard dan pembersihan kipas",
                repairDate,
                ReportStatus.COMPLETED,
                testCreatedAt,
                testUpdatedAt
        );

        // Then
        assertNotNull(laporan);
        assertEquals(testId, laporan.getId());
        assertEquals("Budi", laporan.getTechnicianName());
        assertEquals("Penggantian motherboard dan pembersihan kipas", laporan.getRepairDetails());
        assertEquals(repairDate, laporan.getRepairDate());
        assertEquals(ReportStatus.COMPLETED, laporan.getStatus());
        assertEquals(testCreatedAt, laporan.getCreatedAt());
        assertEquals(testUpdatedAt, laporan.getUpdatedAt());
    }

    @Test
    public void testGetterSetterMethods() {
        // Given
        Report laporan = new Report();
        LocalDate repairDate = LocalDate.now();

        // When
        laporan.setId(testId);
        laporan.setTechnicianName("Budi");
        laporan.setRepairDetails("Penggantian motherboard dan pembersihan kipas");
        laporan.setRepairDate(repairDate);
        laporan.setStatus(ReportStatus.COMPLETED);
        laporan.setCreatedAt(testCreatedAt);
        laporan.setUpdatedAt(testUpdatedAt);

        // Then
        assertEquals(testId, laporan.getId());
        assertEquals("Budi", laporan.getTechnicianName());
        assertEquals("Penggantian motherboard dan pembersihan kipas", laporan.getRepairDetails());
        assertEquals(repairDate, laporan.getRepairDate());
        assertEquals(ReportStatus.COMPLETED, laporan.getStatus());
        assertEquals(testCreatedAt, laporan.getCreatedAt());
        assertEquals(testUpdatedAt, laporan.getUpdatedAt());
    }

    @Test
    public void testReportBuilder_MinimalRequiredFields() {
        // Given
        LocalDate repairDate = LocalDate.now();

        // When
        Report laporan = Report.builder()
                .technicianName("Budi")
                .repairDetails("Perbaikan layar")
                .repairDate(repairDate)
                .status(ReportStatus.IN_PROGRESS)
                .build();

        // Then
        assertNotNull(laporan);
        assertNull(laporan.getId());
        assertEquals("Budi", laporan.getTechnicianName());
        assertEquals("Perbaikan layar", laporan.getRepairDetails());
        assertEquals(repairDate, laporan.getRepairDate());
        assertEquals(ReportStatus.IN_PROGRESS, laporan.getStatus());
        assertNull(laporan.getCreatedAt());
        assertNull(laporan.getUpdatedAt());
    }

    @Test
    public void testReportBuilder_MissingRequiredFields() {
        Report laporan = Report.builder().build();

        assertNull(laporan.getTechnicianName());
        assertNull(laporan.getRepairDetails());
        assertNull(laporan.getRepairDate());
        assertNull(laporan.getStatus());
    }
}
