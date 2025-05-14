package id.ac.ui.cs.advprog.everest.modules.report.repository;

import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    private Report sampleReport;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        sampleReport = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Fixed broken screen")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
    }

    @Test
    void testSaveNewReport() {
        Report saved = reportRepository.save(sampleReport);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTechnicianName()).isEqualTo(sampleReport.getTechnicianName());

        List<Report> all = reportRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void testFindById() {
        Report saved = reportRepository.save(sampleReport);
        UUID id = saved.getId();

        Optional<Report> found = reportRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getTechnicianName()).isEqualTo(sampleReport.getTechnicianName());
    }

    @Test
    void testFindByIdWithNonExistentId() {
        Optional<Report> found = reportRepository.findById(UUID.randomUUID());
        assertThat(found).isNotPresent();
    }

    @Test
    void testUpdateExistingReport() {
        Report saved = reportRepository.save(sampleReport);
        saved.setRepairDetails("Updated details");
        saved.setStatus(ReportStatus.COMPLETED);

        Report updated = reportRepository.save(saved);
        assertThat(updated.getRepairDetails()).isEqualTo("Updated details");
        assertThat(updated.getStatus()).isEqualTo(ReportStatus.COMPLETED);

        List<Report> all = reportRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void testFindByTechnicianNameContainingIgnoreCase() {
        reportRepository.save(sampleReport);
        Report other = Report.builder()
                .technicianName("Alice Johnson")
                .repairDetails("Replaced motherboard")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING_CONFIRMATION)
                .build();
        reportRepository.save(other);

        List<Report> johnDoe = reportRepository.findByTechnicianNameContainingIgnoreCase("John Doe");
        assertThat(johnDoe).hasSize(1);

        List<Report> joh = reportRepository.findByTechnicianNameContainingIgnoreCase("joh");
        assertThat(joh).hasSize(2);

        List<Report> none = reportRepository.findByTechnicianNameContainingIgnoreCase("Bob");
        assertThat(none).isEmpty();
    }

    @Test
    void testFindByStatus() {
        reportRepository.save(sampleReport);
        Report pending = Report.builder()
                .technicianName("Alice")
                .repairDetails("Test")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING_CONFIRMATION)
                .build();
        reportRepository.save(pending);

        List<Report> completed = reportRepository.findByStatus(ReportStatus.COMPLETED);
        assertThat(completed).hasSize(1);

        List<Report> canceled = reportRepository.findByStatus(ReportStatus.CANCELLED);
        assertThat(canceled).isEmpty();
    }

    @Test
    void testFindByTechnicianNameAndStatus() {
        reportRepository.save(sampleReport);
        Report inProgress = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Something else")
                .repairDate(LocalDate.now())
                .status(ReportStatus.PENDING_CONFIRMATION)
                .build();
        reportRepository.save(inProgress);
        Report other = Report.builder()
                .technicianName("Jane")
                .repairDetails("More")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(other);

        List<Report> johnCompleted = reportRepository
                .findByTechnicianNameContainingIgnoreCaseAndStatus("John", ReportStatus.COMPLETED);
        assertThat(johnCompleted).hasSize(1);

        List<Report> johnPending = reportRepository
                .findByTechnicianNameContainingIgnoreCaseAndStatus("John", ReportStatus.PENDING_CONFIRMATION);
        assertThat(johnPending).hasSize(1);

        List<Report> none = reportRepository
                .findByTechnicianNameContainingIgnoreCaseAndStatus("Bob", ReportStatus.CANCELLED);
        assertThat(none).isEmpty();
    }

    @Test
    void testSearchByTechnicianAndStatusQuery() {
        reportRepository.save(sampleReport);
        Report other = Report.builder()
                .technicianName("Johnathan Doe")
                .repairDetails("Other")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(other);

        List<Report> res = reportRepository.searchByTechnicianAndStatus("john", ReportStatus.COMPLETED);
        assertThat(res).hasSize(2);
    }

    @Test
    void testFindByRepairDate() {
        reportRepository.save(sampleReport);
        Report yesterday = Report.builder()
                .technicianName("Jane")
                .repairDetails("Fix")
                .repairDate(LocalDate.now().minusDays(1))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(yesterday);

        List<Report> today = reportRepository.findByRepairDate(LocalDate.now());
        assertThat(today).hasSize(1);

        List<Report> yest = reportRepository.findByRepairDate(LocalDate.now().minusDays(1));
        assertThat(yest).hasSize(1);

        List<Report> none = reportRepository.findByRepairDate(LocalDate.now().minusDays(2));
        assertThat(none).isEmpty();
    }

    @Test
    void testFindByRepairDateBetween() {
        Report today = sampleReport;
        reportRepository.save(today);
        Report yesterday = Report.builder()
                .technicianName("Jane")
                .repairDetails("Fix")
                .repairDate(LocalDate.now().minusDays(1))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(yesterday);
        Report lastWeek = Report.builder()
                .technicianName("Bob")
                .repairDetails("Fix")
                .repairDate(LocalDate.now().minusDays(7))
                .status(ReportStatus.COMPLETED)
                .build();
        reportRepository.save(lastWeek);

        List<Report> recent = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(1), LocalDate.now());
        assertThat(recent).hasSize(2);

        List<Report> allWeek = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(7), LocalDate.now());
        assertThat(allWeek).hasSize(3);

        List<Report> none = reportRepository.findByRepairDateBetween(
                LocalDate.now().minusDays(14), LocalDate.now().minusDays(8));
        assertThat(none).isEmpty();
    }
}
