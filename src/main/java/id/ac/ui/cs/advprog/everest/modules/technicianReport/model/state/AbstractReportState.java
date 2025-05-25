package id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.IllegalAccessTechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;

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
    public ReportState startWork(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "startWork");
    }

    @Override
    public ReportState complete(TechnicianReport context) {
        throw new IllegalStateTransitionException(getName(), "complete");
    }

    @Override
    public boolean technicianCanModify() {
        return false;
    }

    @Override
    public boolean customerCanSee() {
        return true;
    }

    @Override
    public void readPermissions(TechnicianReport context) {
        if (!customerCanSee()) {
            throw new IllegalAccessTechnicianReport("Customer", "see report in draft state");
        }
    }
}