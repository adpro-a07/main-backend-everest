package id.ac.ui.cs.advprog.everest.modules.report.model;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

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
    void testReportBuilder_AllFields() {
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
    void testReportNoArgsConstructor() {
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
    void testReportAllArgsConstructor() {
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
    void testGetterSetterMethods() {
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
    void testReportBuilder_MinimalRequiredFields() {
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
    void testReportBuilder_MissingRequiredFields() {
        Report laporan = Report.builder().build();

        assertNull(laporan.getTechnicianName());
        assertNull(laporan.getRepairDetails());
        assertNull(laporan.getRepairDate());
        assertNull(laporan.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        LocalDate repairDate = LocalDate.now();
        Report report1 = new Report(
                testId,
                "Budi",
                "Penggantian motherboard",
                repairDate,
                ReportStatus.COMPLETED,
                testCreatedAt,
                testUpdatedAt
        );

        Report report2 = new Report(
                testId,
                "Budi",
                "Penggantian motherboard",
                repairDate,
                ReportStatus.COMPLETED,
                testCreatedAt,
                testUpdatedAt
        );

        Report differentReport = new Report(
                UUID.randomUUID(),
                "Andi",
                "Perbaikan layar",
                repairDate,
                ReportStatus.IN_PROGRESS,
                testCreatedAt,
                testUpdatedAt
        );

        // Then - Using Object's default equals and hashCode
        assertNotSame(report1, report2);
        assertNotEquals(report1, differentReport);
    }
}