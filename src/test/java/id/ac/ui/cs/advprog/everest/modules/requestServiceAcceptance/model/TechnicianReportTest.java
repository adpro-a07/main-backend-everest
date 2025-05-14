package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportTest {

    private UUID reportId;
    private UUID technicianId;
    private UUID userId;
    private UserRequest userRequest;
    private String diagnosis;
    private String actionPlan;
    private BigDecimal estimatedCost;
    private Duration estimatedTime;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userRequest = new UserRequest(userId, "Fix my refrigerator");
        diagnosis = "Refrigerator compressor is faulty";
        actionPlan = "Replace compressor and test cooling system";
        estimatedCost = new BigDecimal("350.75");
        estimatedTime = Duration.ofHours(2).plusMinutes(30);
    }

    @Test
    void testCreateTechnicianReportWithConstructor() {
        TechnicianReport report = new TechnicianReport(
                reportId, userRequest, technicianId, diagnosis, actionPlan, estimatedCost, estimatedTime);

        assertEquals(reportId, report.getReportId());
        assertEquals(userRequest, report.getUserRequest());
        assertEquals(technicianId, report.getTechnicianId());
        assertEquals(diagnosis, report.getDiagnosis());
        assertEquals(actionPlan, report.getActionPlan());
        assertEquals(estimatedCost, report.getEstimatedCost());

        // Check Duration conversion
        assertEquals(estimatedTime.getSeconds(), report.getEstimatedTimeSeconds());
        assertEquals(estimatedTime, report.getEstimatedTime());
    }

    @Test
    void testBuilderPattern() {
        // Note: Fixing the method name from "UserRequest" to "userRequest"
        TechnicianReport report = TechnicianReport.builder()
                .reportId(reportId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .diagnosis(diagnosis)
                .actionPlan(actionPlan)
                .estimatedCost(estimatedCost)
                .estimatedTime(estimatedTime)
                .build();

        assertEquals(reportId, report.getReportId());
        assertEquals(userRequest, report.getUserRequest());
        assertEquals(technicianId, report.getTechnicianId());
        assertEquals(diagnosis, report.getDiagnosis());
        assertEquals(actionPlan, report.getActionPlan());
        assertEquals(estimatedCost, report.getEstimatedCost());
        assertEquals(estimatedTime, report.getEstimatedTime());
    }

    @Test
    void testNullReportIdGeneratesUUID() {
        TechnicianReport report = TechnicianReport.builder()
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .build();

        // Call onCreate manually for testing
        report.onCreate();

        assertNotNull(report.getReportId());
    }

    @Test
    void testExistingReportIdRemainsSame() {
        UUID existingId = UUID.randomUUID();

        TechnicianReport report = TechnicianReport.builder()
                .reportId(existingId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .build();

        // Call onCreate manually for testing
        report.onCreate();

        assertEquals(existingId, report.getReportId());
    }

    @Test
    void testNullEstimatedTimeHandling() {
        TechnicianReport report = TechnicianReport.builder()
                .reportId(reportId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .estimatedTime(null)
                .build();

        assertNull(report.getEstimatedTimeSeconds());
        assertNull(report.getEstimatedTime());
    }

    @Test
    void testZeroDuration() {
        Duration zeroDuration = Duration.ZERO;

        TechnicianReport report = TechnicianReport.builder()
                .reportId(reportId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .estimatedTime(zeroDuration)
                .build();

        assertEquals(0L, report.getEstimatedTimeSeconds());
        assertEquals(zeroDuration, report.getEstimatedTime());
    }

    @Test
    void testLargeDuration() {
        Duration largeDuration = Duration.ofDays(30).plusHours(5).plusMinutes(45).plusSeconds(15);

        TechnicianReport report = TechnicianReport.builder()
                .reportId(reportId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .estimatedTime(largeDuration)
                .build();

        assertEquals(largeDuration.getSeconds(), report.getEstimatedTimeSeconds());
        assertEquals(largeDuration, report.getEstimatedTime());
    }

    @Test
    void testNoArgsConstructor() {
        TechnicianReport report = new TechnicianReport();

        assertNull(report.getReportId());
        assertNull(report.getUserRequest());
        assertNull(report.getTechnicianId());
        assertNull(report.getDiagnosis());
        assertNull(report.getActionPlan());
        assertNull(report.getEstimatedCost());
        assertNull(report.getEstimatedTimeSeconds());
        assertNull(report.getEstimatedTime());
    }

    @Test
    void testUserRequestRelationship() {
        TechnicianReport report = TechnicianReport.builder()
                .reportId(reportId)
                .UserRequest(userRequest)
                .technicianId(technicianId)
                .build();

        assertEquals(userRequest.getRequestId(), report.getUserRequest().getRequestId());
        assertEquals(userRequest.getUserId(), report.getUserRequest().getUserId());
        assertEquals(userRequest.getUserDescription(), report.getUserRequest().getUserDescription());
    }
}