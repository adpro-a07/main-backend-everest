package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.model.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReportRepositoryTest {

    private ReportRepository reportRepository;
    private Report sampleReport;

    @BeforeEach
    void setUp() {
        reportRepository = new ReportRepository();

        sampleReport = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Fixed broken screen")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
    }

    @Test
    void testSaveNewReport() {
        Report savedReport = reportRepository.save(sampleReport);

        assertNotNull(savedReport);
        assertNotNull(savedReport.getId());
        assertEquals(1L, savedReport.getId());
        assertEquals(sampleReport.getTechnicianName(), savedReport.getTechnicianName());

        List<Report> allReports = reportRepository.findAll();
        assertEquals(1, allReports.size());
    }

    @Test
    void testAutoIncrementId() {
        Report firstReport = reportRepository.save(sampleReport);

        Report secondReport = Report.builder()
                .technicianName("Jane Smith")
                .repairDetails("Fixed charging port")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();

        Report savedSecondReport = reportRepository.save(secondReport);

        assertEquals(1L, firstReport.getId());
        assertEquals(2L, savedSecondReport.getId());
    }

    @Test
    void testFindById() {
        Report savedReport = reportRepository.save(sampleReport);

        Optional<Report> foundReport = reportRepository.findById(savedReport.getId());

        assertTrue(foundReport.isPresent());
        assertEquals(sampleReport.getTechnicianName(), foundReport.get().getTechnicianName());
    }

    @Test
    void testFindByIdWithNonExistentId() {
        Optional<Report> foundReport = reportRepository.findById(999);

        assertFalse(foundReport.isPresent());
    }

    @Test
    void testUpdateExistingReport() {
        Report savedReport = reportRepository.save(sampleReport);

        savedReport.setRepairDetails("Fixed broken screen and replaced battery");
        savedReport.setStatus(ReportStatus.COMPLETED);

        Report updatedReport = reportRepository.save(savedReport);

        assertEquals("Fixed broken screen and replaced battery", updatedReport.getRepairDetails());
        assertEquals(ReportStatus.COMPLETED, updatedReport.getStatus());

        List<Report> allReports = reportRepository.findAll();
        assertEquals(1, allReports.size());
    }

    @Test
    void testDeleteById() {
        Report savedReport = reportRepository.save(sampleReport);

        reportRepository.deleteById(savedReport.getId());

        List<Report> allReports = reportRepository.findAll();
        assertEquals(0, allReports.size());

        Optional<Report> foundReport = reportRepository.findById(savedReport.getId());
        assertFalse(foundReport.isPresent());
    }

    @Test
    void testFindByTechnicianNameContainingIgnoreCase() {
        reportRepository.save(sampleReport);

        Report anotherReport = Report.builder()
                .technicianName("Alice Johnson")
                .repairDetails("Replaced motherboard")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(anotherReport);

        List<Report> johnReports = reportRepository.findByTechnicianNameContainingIgnoreCase("John Doe");
        assertEquals(1, johnReports.size());

        List<Report> johReports = reportRepository.findByTechnicianNameContainingIgnoreCase("joh");
        assertEquals(2, johReports.size());

        List<Report> noReports = reportRepository.findByTechnicianNameContainingIgnoreCase("Bob");
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByStatusIgnoreCase() {
        reportRepository.save(sampleReport);

        Report anotherReport = Report.builder()
                .technicianName("Alice Johnson")
                .repairDetails("Replaced motherboard")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(anotherReport);

        List<Report> completedReports = reportRepository.findByStatus(ReportStatus.COMPLETED);
        assertEquals(1, completedReports.size());

        List<Report> noReports = reportRepository.findByStatus(ReportStatus.REJECTED);
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase() {
        reportRepository.save(sampleReport);

        Report report2 = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Repaired keyboard")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(report2);

        Report report3 = Report.builder()
                .technicianName("Jane Smith")
                .repairDetails("Fixed display")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(report3);

        List<Report> johnCompletedReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus("John", ReportStatus.COMPLETED);
        assertEquals(1, johnCompletedReports.size());

        List<Report> johnInProgressReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus("John", ReportStatus.PENDING);
        assertEquals(1, johnInProgressReports.size());

        List<Report> noReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus("Bob", ReportStatus.REJECTED);
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByRepairDate() {
        reportRepository.save(sampleReport);

        Report yesterdayReport = Report.builder()
                .technicianName("Jane Smith")
                .repairDetails("Fixed display")
                .repairDate(LocalDate.now().minusDays(1))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(yesterdayReport);

        List<Report> todayReports = reportRepository.findByRepairDate(LocalDate.now());
        assertEquals(1, todayReports.size());

        List<Report> yesterdayReports = reportRepository.findByRepairDate(LocalDate.now().minusDays(1));
        assertEquals(1, yesterdayReports.size());

        List<Report> noReports = reportRepository.findByRepairDate(LocalDate.now().minusDays(2));
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByRepairDateBetween() {
        Report todayReport = sampleReport;
        reportRepository.save(todayReport);

        Report yesterdayReport = Report.builder()
                .technicianName("Jane Smith")
                .repairDetails("Fixed display")
                .repairDate(LocalDate.now().minusDays(1))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(yesterdayReport);

        Report lastWeekReport = Report.builder()
                .technicianName("Bob Brown")
                .repairDetails("Replaced hard drive")
                .repairDate(LocalDate.now().minusDays(7))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(lastWeekReport);

        List<Report> recentReports = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(2, recentReports.size());

        List<Report> weekReports = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertEquals(3, weekReports.size());

        List<Report> noReports = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(14), LocalDate.now().minusDays(8));
        assertEquals(0, noReports.size());
    }
}