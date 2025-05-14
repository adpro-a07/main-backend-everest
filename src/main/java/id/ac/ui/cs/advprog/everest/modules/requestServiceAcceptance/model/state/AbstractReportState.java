// Abstract base state implementation
package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.state;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

public abstract class AbstractReportState implements ReportState {
    @Override
    public ReportState submit(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "submit");
    }

    @Override
    public ReportState approve(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "approve");
    }

    @Override
    public ReportState reject(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "reject");
    }

    @Override
    public ReportState revise(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "revise");
    }

    @Override
    public ReportState startWork(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "startWork");
    }

    @Override
    public ReportState complete(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "complete");
    }

    @Override
    public boolean canEdit() {
        return false;
    }
}