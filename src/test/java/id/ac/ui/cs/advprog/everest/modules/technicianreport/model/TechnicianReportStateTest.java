package id.ac.ui.cs.advprog.everest.modules.technicianreport.model;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianReportStateTest {
    private UUID reportId;
    private UUID technicianId;
    private RepairOrder repairOrder;
    private TechnicianReport technicianReport;

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
    void testDraftStateTransitionsToSubmitted() {
        DraftState draftState = new DraftState();

        ReportState newState = draftState.submit(technicianReport);

        assertTrue(newState instanceof SubmittedState);
    }

    @Test
    void testDraftStateValidationBeforeSubmit() {
        TechnicianReport incompleteReport = TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .repairOrder(repairOrder)
                .technicianId(UUID.randomUUID())
                .build();

        DraftState draftState = new DraftState();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> draftState.submit(incompleteReport)
        );
        assertTrue(exception.getMessage().contains("required before submitting"));
    }

    @Test
    void testSubmittedStateTransitionsToApproved() {
        SubmittedState submittedState = new SubmittedState();

        ReportState newState = submittedState.approve(technicianReport);

        assertTrue(newState instanceof ApprovedState);
    }

    @Test
    void testSubmittedStateTransitionsToRejected() {
        SubmittedState submittedState = new SubmittedState();

        ReportState newState = submittedState.reject(technicianReport);

        assertTrue(newState instanceof RejectedState);
    }

    @Test
    void testApprovedStateTransitionsToInProgress() {
        ApprovedState approvedState = new ApprovedState();

        ReportState newState = approvedState.startWork(technicianReport);

        assertTrue(newState instanceof InProgressState);
    }

    @Test
    void testInProgressStateTransitionsToCompleted() {
        InProgressState inProgressState = new InProgressState();

        ReportState newState = inProgressState.complete(technicianReport);

        assertTrue(newState instanceof CompletedState);
    }

    @Test
    void testInvalidStateTransition_SubmitFromNonDraftState() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("SUBMITTED"); // Already in SUBMITTED state
        technicianReport1.initializeState();

        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                technicianReport1::submit
        );
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Cannot perform SUBMITTED action while in submit state"));
    }

    @Test
    void testInvalidStateTransition_RejectFromNonSubmittedState() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();

        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                technicianReport1::reject
        );

        assertTrue(exception.getMessage().contains("Cannot perform DRAFT action while in reject state"));
    }

    @Test
    void testInvalidStateTransition_ApproveFromNonSubmittedState() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();

        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                technicianReport1::approve
        );

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Cannot perform DRAFT action while in approve state"));
    }

    @Test
    void testInvalidStateTransition_StartWorkFromNonApprovedState() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();

        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                technicianReport1::startWork
        );

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Cannot perform DRAFT action while in start work state"));
    }

    @Test
    void testInvalidStateTransition_CompleteFromNonInProgressState() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("APPROVED");
        technicianReport1.initializeState();

        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                technicianReport1::complete
        );

        assertTrue(exception.getMessage().contains("Cannot perform APPROVED action while in complete state"));
    }

    @Test
    void testValidationFailure_SubmitWithoutDiagnosis() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();
        technicianReport1.setDiagnosis(null);
        technicianReport1.setActionPlan("Valid action plan");
        technicianReport1.setEstimatedCost(100L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                technicianReport1::submit
        );

        assertTrue(exception.getMessage().contains("Diagnosis is required"));
    }

    @Test
    void testValidationFailure_SubmitWithoutActionPlan() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();
        technicianReport1.setDiagnosis("Valid diagnosis");
        technicianReport1.setActionPlan(null);
        technicianReport1.setEstimatedCost(100L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                technicianReport1::submit
        );

        assertTrue(exception.getMessage().contains("Action plan is required"));
    }

    @Test
    void testValidationFailure_SubmitWithoutEstimatedCost() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();
        technicianReport1.setDiagnosis("Valid diagnosis");
        technicianReport1.setActionPlan("Valid action plan");
        technicianReport1.setEstimatedCost(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                technicianReport1::submit
        );

        assertTrue(exception.getMessage().contains("Estimated cost is required"));
    }

    @Test
    void testValidationFailure_StartWorkWithoutEstimatedTime() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("APPROVED");
        technicianReport1.initializeState();
        technicianReport1.setEstimatedTimeSeconds(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                technicianReport1::startWork
        );

        assertTrue(exception.getMessage().contains("Estimated time is required"));
    }

    @Test
    void testCustomerCannotSeeDraftReports() {
        TechnicianReport technicianReport1 = new TechnicianReport();
        technicianReport1.setStatus("DRAFT");
        technicianReport1.initializeState();

        assertFalse(technicianReport1.customerCanSee());
    }
}
