package id.ac.ui.cs.advprog.everest.modules.technicianReport.model;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportTest {

    private TechnicianReport report;
    private UUID reportId;
    private UUID technicianId;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        userRequest = new UserRequest(UUID.randomUUID(), "Fix my refrigerator");
        report = TechnicianReport.builder()
                .reportId(reportId)
                .userRequest(userRequest)
                .technicianId(technicianId)
                .diagnosis("Compressor issue")
                .actionPlan("Replace compressor")
                .estimatedCost(new BigDecimal("300.00"))
                .estimatedTime(Duration.ofHours(2))
                .build();
    }

    @Test
    void testInitialStateIsDraft() {
        assertEquals("DRAFT", report.getStatus());
        assertTrue(report.canEdit());
    }

    @Test
    void testSubmitTransition() {
        report.submit();
        assertEquals("SUBMITTED", report.getStatus());
        assertFalse(report.canEdit());
    }

    @Test
    void testSubmitWithoutDiagnosisThrowsException() {
        report = TechnicianReport.builder()
                .userRequest(userRequest)
                .technicianId(technicianId)
                .build();

        assertThrows(IllegalStateException.class, report::submit);
    }

    @Test
    void testApproveTransition() {
        report.submit();
        report.approve();
        assertEquals("APPROVED", report.getStatus());
    }

    @Test
    void testRejectTransition() {
        report.submit();
        report.reject();
        assertEquals("REJECTED", report.getStatus());
    }

    @Test
    void testStartWorkTransition() {
        report.submit();
        report.approve();
        report.startWork();
        assertEquals("IN_PROGRESS", report.getStatus());
    }

    @Test
    void testCompleteWorkTransition() {
        report.submit();
        report.approve();
        report.startWork();
        report.complete();
        assertEquals("COMPLETED", report.getStatus());
    }

    @Test
    void testInvalidTransitionThrowsException() {
        assertThrows(IllegalStateTransitionException.class, report::approve);
        assertThrows(IllegalStateTransitionException.class, report::startWork);
    }
}