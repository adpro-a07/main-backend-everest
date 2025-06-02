package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;

public class InProgressState extends AbstractReportState implements CompletableState {

    @Override
    public String getName() {
        return ReportConstants.IN_PROGRESS;
    }

    @Override
    public ReportState complete(TechnicianReport context) {
        return new CompletedState();
    }
}