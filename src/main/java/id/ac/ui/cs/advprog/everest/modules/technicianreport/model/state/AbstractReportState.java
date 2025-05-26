package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.exception.IllegalAccessTechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public abstract class AbstractReportState implements ReportState {

    @Override
    public boolean technicianCanModify() {
        return false;
    }

    @Override
    public boolean customerCanSee() {
        return true;
    }

    @Override
    public void validateReadPermissions(TechnicianReport context) {
        if (!customerCanSee()) {
            throw new IllegalAccessTechnicianReport("Customer", "see report in " + getName().toLowerCase() + " state");
        }
    }
}