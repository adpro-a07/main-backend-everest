package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.state;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

public class ApprovedState extends AbstractReportState {
    @Override
    public String getName() {
        return "APPROVED";
    }

    @Override
    public ReportState startWork(TechnicianReport context) {
        return new InProgressState();
    }
}