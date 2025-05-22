package id.ac.ui.cs.advprog.everest.modules.technicianReport.model;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportTest {
    private TechnicianReport report;
    private UUID reportId;
    private UUID technicianId;
    private RepairOrder repairOrder;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        repairOrder = RepairOrder.builder()
                .customerId(UUID.randomUUID())
                .technicianId(technicianId)
                .itemName("Item Name")
                .itemCondition("Item Condition")
                .issueDescription("Issue Description")
                .status(RepairOrderStatus.PENDING_CONFIRMATION)
                .build();
        report = TechnicianReport.builder()
                .reportId(reportId)
                .repairOrder(repairOrder)
                .technicianId(technicianId)
                .diagnosis("Compressor issue")
                .actionPlan("Replace compressor")
                .estimatedCost(300L)
                .estimatedTimeSeconds(3600L)
                .build();
    }

    @Test
    void testInitialStateIsDraft() {
        assertEquals("DRAFT", report.getStatus());
        assertTrue(report.technicianCanModify());
    }

    @Test
    void testSubmitTransition() {
        report.submit();
        assertEquals("SUBMITTED", report.getStatus());
        assertFalse(report.technicianCanModify());
    }

    @Test
    void testSubmitWithoutDiagnosisThrowsException() {
        report = TechnicianReport.builder()
                .repairOrder(repairOrder)
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

    @Test
    void testOnCreateGeneratesReportId() {
        TechnicianReport report = new TechnicianReport();
        report.setReportId(null);

        report.onCreate();

        assertNotNull(report.getReportId());
    }

    @Test
    void testInitializeStateWithNullStatus() {
        TechnicianReport report = new TechnicianReport();
        report.setStatus(null);

        report.initializeState(); // Make this method package-private instead of private

        assertTrue(report.getState() instanceof DraftState);
    }

    @Test
    void testInitializeStateWithAllStatuses() {
        TechnicianReport report = new TechnicianReport();

        report.setStatus("DRAFT");
        report.initializeState();
        assertTrue(report.getState() instanceof DraftState);

        report.setStatus("SUBMITTED");
        report.initializeState();
        assertTrue(report.getState() instanceof SubmittedState);

        report.setStatus("APPROVED");
        report.initializeState();
        assertTrue(report.getState() instanceof ApprovedState);

        report.setStatus("REJECTED");
        report.initializeState();
        assertTrue(report.getState() instanceof RejectedState);

        report.setStatus("IN_PROGRESS");
        report.initializeState();
        assertTrue(report.getState() instanceof InProgressState);

        report.setStatus("COMPLETED");
        report.initializeState();
        assertTrue(report.getState() instanceof CompletedState);
    }

    @Test
    void testInitializeStateWithInvalidStatus() {
        TechnicianReport report = new TechnicianReport();
        report.setStatus("INVALID_STATUS");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> report.initializeState());
        assertTrue(exception.getMessage().contains("Unknown status: INVALID_STATUS"));
    }

    @Test
    void testOnCreateInitializesLastUpdatedAt() {
        TechnicianReport report = new TechnicianReport();
        report.setLastUpdatedAt(null);

        report.onCreate();

        assertNotNull(report.getLastUpdatedAt());
    }

    @Test
    void testGetEstimatedTimeWithNull() {
        TechnicianReport report = new TechnicianReport();
        report.setEstimatedTimeSeconds(null);

        assertNull(report.getEstimatedTime());
    }
}