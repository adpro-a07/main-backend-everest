package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.service.ReportValidationService;

public class SubmittedState extends AbstractReportState implements ReviewableState {

    @Override
    public String getName() {
        return ReportConstants.SUBMITTED;
    }

    @Override
    public ReportState approve(TechnicianReport context) {
        ReportValidationService.validateForApproval(context);
        return new ApprovedState();
    }

    @Override
    public ReportState reject(TechnicianReport context) {
        return new RejectedState();
    }
}