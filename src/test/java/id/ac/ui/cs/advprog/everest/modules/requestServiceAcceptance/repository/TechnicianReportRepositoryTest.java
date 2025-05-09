package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.TechnicianReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TechnicianReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TechnicianReportRepository technicianReportRepository;

    @Test
    public void testSaveTechnicianReport() {
        TechnicianReport report = TechnicianReport.builder()
                .requestId(1L)
                .technicianId(2L)
                .diagnosis("Hard drive failure")
                .actionPlan("Replace hard drive and reinstall OS")
                .estimatedCost(new BigDecimal("150.00"))
                .estimatedTime(Duration.ofHours(2))
                .build();

        TechnicianReport savedReport = technicianReportRepository.save(report);

        assertNotNull(savedReport.getReportId());
        assertEquals(1L, savedReport.getRequestId());
        assertEquals(2L, savedReport.getTechnicianId());
        assertEquals("Hard drive failure", savedReport.getDiagnosis());
        assertEquals("Replace hard drive and reinstall OS", savedReport.getActionPlan());
        assertEquals(new BigDecimal("150.00"), savedReport.getEstimatedCost());
        assertEquals(Duration.ofHours(2), savedReport.getEstimatedTime());
    }

    @Test
    public void testFindByRequestId() {
        TechnicianReport report1 = TechnicianReport.builder()
                .requestId(1L)
                .technicianId(2L)
                .diagnosis("RAM issues")
                .actionPlan("Replace RAM")
                .estimatedCost(new BigDecimal("75.00"))
                .estimatedTime(Duration.ofMinutes(30))
                .build();

        TechnicianReport report2 = TechnicianReport.builder()
                .requestId(2L)
                .technicianId(2L)
                .diagnosis("Power supply failure")
                .actionPlan("Replace power supply")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.flush();

        Optional<TechnicianReport> foundReport = technicianReportRepository.findByRequestId(1L);
        Optional<TechnicianReport> notFoundReport = technicianReportRepository.findByRequestId(999L);

        assertTrue(foundReport.isPresent());
        assertEquals("RAM issues", foundReport.get().getDiagnosis());
        assertEquals(1L, foundReport.get().getRequestId());

        assertFalse(notFoundReport.isPresent());
    }

    @Test
    public void testFindByTechnicianId() {
        Long technicianId = 3L;

        TechnicianReport report1 = TechnicianReport.builder()
                .requestId(1L)
                .technicianId(technicianId)
                .diagnosis("Diagnosis 1")
                .actionPlan("Action plan 1")
                .estimatedCost(new BigDecimal("50.00"))
                .estimatedTime(Duration.ofMinutes(45))
                .build();

        TechnicianReport report2 = TechnicianReport.builder()
                .requestId(2L)
                .technicianId(technicianId)
                .diagnosis("Diagnosis 2")
                .actionPlan("Action plan 2")
                .estimatedCost(new BigDecimal("120.00"))
                .estimatedTime(Duration.ofHours(3))
                .build();

        TechnicianReport report3 = TechnicianReport.builder()
                .requestId(3L)
                .technicianId(4L) // Different technician
                .diagnosis("Diagnosis 3")
                .actionPlan("Action plan 3")
                .estimatedCost(new BigDecimal("200.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        List<TechnicianReport> technicianReports = technicianReportRepository.findByTechnicianId(technicianId);

        assertEquals(2, technicianReports.size());
        assertTrue(technicianReports.stream().allMatch(report -> report.getTechnicianId().equals(technicianId)));
        assertFalse(technicianReports.stream().anyMatch(report -> report.getTechnicianId().equals(4L)));
    }

    @Test
    public void testUpdateTechnicianReport() {
        TechnicianReport report = TechnicianReport.builder()
                .requestId(1L)
                .technicianId(2L)
                .diagnosis("Initial diagnosis")
                .actionPlan("Initial action plan")
                .estimatedCost(new BigDecimal("100.00"))
                .estimatedTime(Duration.ofHours(1))
                .build();

        entityManager.persist(report);
        entityManager.flush();

        TechnicianReport updatedReport = TechnicianReport.builder()
                .reportId(report.getReportId())
                .requestId(report.getRequestId())
                .technicianId(report.getTechnicianId())
                .diagnosis("Updated diagnosis")
                .actionPlan("Updated action plan")
                .estimatedCost(new BigDecimal("150.00"))
                .estimatedTime(Duration.ofHours(2))
                .build();

        technicianReportRepository.save(updatedReport);

        Optional<TechnicianReport> retrievedReport = technicianReportRepository.findById(report.getReportId());
        assertTrue(retrievedReport.isPresent());
        assertEquals("Updated diagnosis", retrievedReport.get().getDiagnosis());
        assertEquals("Updated action plan", retrievedReport.get().getActionPlan());
        assertEquals(new BigDecimal("150.00"), retrievedReport.get().getEstimatedCost());
        assertEquals(Duration.ofHours(2), retrievedReport.get().getEstimatedTime());
    }

    @Test
    public void testDeleteTechnicianReport() {
        TechnicianReport report = TechnicianReport.builder()
                .requestId(1L)
                .technicianId(2L)
                .diagnosis("Report to delete")
                .actionPlan("Action plan")
                .estimatedCost(new BigDecimal("80.00"))
                .estimatedTime(Duration.ofMinutes(45))
                .build();

        entityManager.persist(report);
        entityManager.flush();

        Long reportId = report.getReportId();

        technicianReportRepository.deleteById(reportId);

        Optional<TechnicianReport> deletedReport = technicianReportRepository.findById(reportId);
        assertFalse(deletedReport.isPresent());
    }
}