package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.service.ReportValidationService;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;

public class DraftState extends AbstractReportState implements SubmittableState {

    @Override
    public String getName() {
        return ReportConstants.DRAFT;
    }

    @Override
    public ReportState submit(TechnicianReport context) {
        ReportValidationService.validateForSubmission(context);
        return new SubmittedState();
    }

    @Override
    public boolean technicianCanModify() {
        return true;
    }

    @Override
    public boolean customerCanSee() {
        return false;
    }
}