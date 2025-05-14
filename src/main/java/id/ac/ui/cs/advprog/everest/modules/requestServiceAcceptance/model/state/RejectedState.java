package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.state;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

public class RejectedState extends AbstractReportState {
    @Override
    public String getName() {
        return "REJECTED";
    }

    // Override the revise method to prevent any revisions
    // The AbstractReportState already throws IllegalStateTransitionException for revise,
    // but we'll make it more explicit with a clearer message
    @Override
    public ReportState revise(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "revise");
    }

    @Override
    public boolean canEdit() {
        // We also need to set canEdit to false since rejected reports can't be revised
        return false;
    }
}