package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportTest {
    @Test
    void testCreateTechnicianReport() {
        // Arrange
        Long reportId = 1L;
        Long requestId = 100L;
        Long technicianId = 200L;
        String diagnosis = "Broken motor";
        String actionPlan = "Replace motor and test";
        BigDecimal estimatedCost = new BigDecimal("150.00");
        Duration estimatedTime = Duration.ofHours(2);

        // Act
        TechnicianReport report = new TechnicianReport(
                reportId, requestId, technicianId, diagnosis, actionPlan,
                estimatedCost, estimatedTime
        );

        // Assert
        assertEquals(reportId, report.getReportId());
        assertEquals(requestId, report.getRequestId());
        assertEquals(technicianId, report.getTechnicianId());
        assertEquals(diagnosis, report.getDiagnosis());
        assertEquals(actionPlan, report.getActionPlan());
        assertEquals(estimatedCost, report.getEstimatedCost());
        assertEquals(estimatedTime, report.getEstimatedTime());
    }

    @Test
    void testTechnicianReportBuilder() {
        // Arrange & Act
        TechnicianReport report = TechnicianReport.builder()
                .reportId(1L)
                .requestId(100L)
                .technicianId(200L)
                .diagnosis("Broken motor")
                .actionPlan("Replace motor and test")
                .estimatedCost(new BigDecimal("150.00"))
                .estimatedTime(Duration.ofHours(2))
                .build();

        // Assert
        assertEquals(1L, report.getReportId());
        assertEquals(100L, report.getRequestId());
        assertEquals("Broken motor", report.getDiagnosis());
        assertEquals(new BigDecimal("150.00"), report.getEstimatedCost());
    }
}