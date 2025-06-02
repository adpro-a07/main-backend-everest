package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;

public class RejectedState extends AbstractReportState {
    @Override
    public String getName() {
        return ReportConstants.REJECTED;
    }
}