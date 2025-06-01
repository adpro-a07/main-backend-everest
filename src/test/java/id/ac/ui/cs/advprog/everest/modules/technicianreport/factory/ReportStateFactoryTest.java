package id.ac.ui.cs.advprog.everest.modules.technicianreport.factory;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportStateFactoryTest {

    @Test
    void testStateFactoryCreatesDraftState() {
        ReportState state = ReportStateFactory.createState("DRAFT");
        assertTrue(state instanceof DraftState);
    }

    @Test
    void testStateFactoryCreatesAllStates() {
        assertTrue(ReportStateFactory.createState("DRAFT") instanceof DraftState);
        assertTrue(ReportStateFactory.createState("SUBMITTED") instanceof SubmittedState);
        assertTrue(ReportStateFactory.createState("APPROVED") instanceof ApprovedState);
        assertTrue(ReportStateFactory.createState("REJECTED") instanceof RejectedState);
        assertTrue(ReportStateFactory.createState("IN_PROGRESS") instanceof InProgressState);
        assertTrue(ReportStateFactory.createState("COMPLETED") instanceof CompletedState);
    }

    @Test
    void testStateFactoryThrowsExceptionForInvalidState() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReportStateFactory.createState("INVALID_STATE")
        );
        assertTrue(exception.getMessage().contains("Unknown status"));
    }
}
