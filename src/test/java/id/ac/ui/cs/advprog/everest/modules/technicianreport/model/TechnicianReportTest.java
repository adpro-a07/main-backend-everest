package id.ac.ui.cs.advprog.everest.modules.technicianreport.model;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportTest {
    private TechnicianReport technicianReport;
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
        technicianReport = TechnicianReport.builder()
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
        assertEquals("DRAFT", technicianReport.getStatus());
        assertTrue(technicianReport.technicianCanModify());
    }

    @Test
    void testSubmitTransition() {
        technicianReport.submit();
        assertEquals("SUBMITTED", technicianReport.getStatus());
        assertFalse(technicianReport.technicianCanModify());
    }

    @Test
    void testSubmitWithoutDiagnosisThrowsException() {
        technicianReport = TechnicianReport.builder()
                .repairOrder(repairOrder)
                .technicianId(technicianId)
                .build();

        assertThrows(IllegalStateException.class, technicianReport::submit);
    }

    @Test
    void testApproveTransition() {
        technicianReport.submit();
        technicianReport.approve();
        assertEquals("APPROVED", technicianReport.getStatus());
    }

    @Test
    void testRejectTransition() {
        technicianReport.submit();
        technicianReport.reject();
        assertEquals("REJECTED", technicianReport.getStatus());
    }

    @Test
    void testStartWorkTransition() {
        technicianReport.submit();
        technicianReport.approve();
        technicianReport.startWork();
        assertEquals("IN_PROGRESS", technicianReport.getStatus());
    }

    @Test
    void testCompleteWorkTransition() {
        technicianReport.submit();
        technicianReport.approve();
        technicianReport.startWork();
        technicianReport.complete();
        assertEquals("COMPLETED", technicianReport.getStatus());
    }

    @Test
    void testInvalidTransitionThrowsException() {
        assertThrows(IllegalStateTransitionException.class, technicianReport::approve);
        assertThrows(IllegalStateTransitionException.class, technicianReport::startWork);
    }

    @Test
    void testOnCreateGeneratesReportId() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setReportId(null);

        technicianReport1.onCreate();

        assertNotNull(technicianReport1.getReportId());
    }

    @Test
    void testInitializeStateWithNullStatus() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus(null);

        technicianReport1.initializeState(); // Make this method package-private instead of private

        assertTrue(technicianReport1.getState() instanceof DraftState);
    }

    @Test
    void testInitializeStateWithAllStatuses() {
        TechnicianReport technicianReport1 = new TechnicianReport();

        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof DraftState);

        technicianReport1.setStatus("SUBMITTED");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof SubmittedState);

        technicianReport1.setStatus("APPROVED");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof ApprovedState);

        technicianReport1.setStatus("REJECTED");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof RejectedState);

        technicianReport1.setStatus("IN_PROGRESS");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof InProgressState);

        technicianReport1.setStatus("COMPLETED");
        technicianReport1.initializeState();
        assertTrue(technicianReport1.getState() instanceof CompletedState);
    }

    @Test
    void testInitializeStateWithInvalidStatus() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("INVALID_STATUS");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> technicianReport1.initializeState());
        assertTrue(exception.getMessage().contains("Unknown status: INVALID_STATUS"));
    }

    @Test
    void testOnCreateInitializesLastUpdatedAt() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setLastUpdatedAt(null);

        technicianReport1.onCreate();

        assertNotNull(technicianReport1.getLastUpdatedAt());
    }

    @Test
    void testGetEstimatedTimeWithNull() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setEstimatedTimeSeconds(null);

        assertNull(technicianReport1.getEstimatedTime());
    }
}