package id.ac.ui.cs.advprog.everest.modules.technicianreport.model;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportStateMapperTest {

    @Test
    void testStateFactoryCreatesDraftState() {
        ReportState state = ReportStateMapper.createState("DRAFT");
        assertTrue(state instanceof DraftState);
    }

    @Test
    void testStateFactoryCreatesAllStates() {
        assertTrue(ReportStateMapper.createState("DRAFT") instanceof DraftState);
        assertTrue(ReportStateMapper.createState("SUBMITTED") instanceof SubmittedState);
        assertTrue(ReportStateMapper.createState("APPROVED") instanceof ApprovedState);
        assertTrue(ReportStateMapper.createState("REJECTED") instanceof RejectedState);
        assertTrue(ReportStateMapper.createState("IN_PROGRESS") instanceof InProgressState);
        assertTrue(ReportStateMapper.createState("COMPLETED") instanceof CompletedState);
    }

    @Test
    void testStateFactoryThrowsExceptionForInvalidState() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReportStateMapper.createState("INVALID_STATE")
        );
        assertTrue(exception.getMessage().contains("Unknown status"));
    }
}
