package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.state;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

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