package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public class InProgressState extends AbstractReportState {
    @Override
    public String getName() {
        return "IN_PROGRESS";
    }

    @Override
    public ReportState complete(TechnicianReport context) {
        return new CompletedState();
    }
}