package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public class SubmittedState extends AbstractReportState {
    @Override
    public String getName() {
        return "SUBMITTED";
    }

    @Override
    public ReportState approve(TechnicianReport context) {
        return new ApprovedState();
    }

    @Override
    public ReportState reject(TechnicianReport context) {
        return new RejectedState();
    }
}