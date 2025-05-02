package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Report;
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
                .detailPengerjaan("Fixed broken screen")
                .tanggalPengerjaan(LocalDate.now())
                .status("Completed")
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
                .detailPengerjaan("Fixed charging port")
                .tanggalPengerjaan(LocalDate.now())
                .status("Completed")
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
        Optional<Report> foundReport = reportRepository.findById(999L);

        assertFalse(foundReport.isPresent());
    }

    @Test
    void testUpdateExistingReport() {
        Report savedReport = reportRepository.save(sampleReport);

        savedReport.setDetailPengerjaan("Fixed broken screen and replaced battery");
        savedReport.setStatus("Completed and Verified");

        Report updatedReport = reportRepository.save(savedReport);

        assertEquals("Fixed broken screen and replaced battery", updatedReport.getDetailPengerjaan());
        assertEquals("Completed and Verified", updatedReport.getStatus());

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
                .detailPengerjaan("Replaced motherboard")
                .tanggalPengerjaan(LocalDate.now())
                .status("In Progress")
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
                .detailPengerjaan("Replaced motherboard")
                .tanggalPengerjaan(LocalDate.now())
                .status("In Progress")
                .build();
        reportRepository.save(anotherReport);

        List<Report> completedReports = reportRepository.findByStatusIgnoreCase("completed");
        assertEquals(1, completedReports.size());

        List<Report> noReports = reportRepository.findByStatusIgnoreCase("Cancelled");
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase() {
        reportRepository.save(sampleReport);

        Report report2 = Report.builder()
                .technicianName("John Doe")
                .detailPengerjaan("Repaired keyboard")
                .tanggalPengerjaan(LocalDate.now())
                .status("In Progress")
                .build();
        reportRepository.save(report2);

        Report report3 = Report.builder()
                .technicianName("Jane Smith")
                .detailPengerjaan("Fixed display")
                .tanggalPengerjaan(LocalDate.now())
                .status("Completed")
                .build();
        reportRepository.save(report3);

        List<Report> johnCompletedReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase("John", "Completed");
        assertEquals(1, johnCompletedReports.size());

        List<Report> johnInProgressReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase("John", "In Progress");
        assertEquals(1, johnInProgressReports.size());

        List<Report> noReports =
                reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase("Bob", "Cancelled");
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByTanggalPengerjaan() {
        reportRepository.save(sampleReport);

        Report yesterdayReport = Report.builder()
                .technicianName("Jane Smith")
                .detailPengerjaan("Fixed display")
                .tanggalPengerjaan(LocalDate.now().minusDays(1))
                .status("Completed")
                .build();
        reportRepository.save(yesterdayReport);

        List<Report> todayReports = reportRepository.findByTanggalPengerjaan(LocalDate.now());
        assertEquals(1, todayReports.size());

        List<Report> yesterdayReports = reportRepository.findByTanggalPengerjaan(LocalDate.now().minusDays(1));
        assertEquals(1, yesterdayReports.size());

        List<Report> noReports = reportRepository.findByTanggalPengerjaan(LocalDate.now().minusDays(2));
        assertEquals(0, noReports.size());
    }

    @Test
    void testFindByTanggalPengerjaanBetween() {
        Report todayReport = sampleReport;
        reportRepository.save(todayReport);

        Report yesterdayReport = Report.builder()
                .technicianName("Jane Smith")
                .detailPengerjaan("Fixed display")
                .tanggalPengerjaan(LocalDate.now().minusDays(1))
                .status("Completed")
                .build();
        reportRepository.save(yesterdayReport);

        Report lastWeekReport = Report.builder()
                .technicianName("Bob Brown")
                .detailPengerjaan("Replaced hard drive")
                .tanggalPengerjaan(LocalDate.now().minusDays(7))
                .status("Completed")
                .build();
        reportRepository.save(lastWeekReport);

        List<Report> recentReports = reportRepository.findByTanggalPengerjaanBetween(
                LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(2, recentReports.size());

        List<Report> weekReports = reportRepository.findByTanggalPengerjaanBetween(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertEquals(3, weekReports.size());

        List<Report> noReports = reportRepository.findByTanggalPengerjaanBetween(
                LocalDate.now().minusDays(14), LocalDate.now().minusDays(8));
        assertEquals(0, noReports.size());
    }
}