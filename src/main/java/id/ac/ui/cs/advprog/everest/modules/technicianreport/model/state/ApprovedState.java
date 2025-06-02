package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.service.ReportValidationService;

public class ApprovedState extends AbstractReportState implements WorkableState {

    @Override
    public String getName() {
        return ReportConstants.APPROVED;
    }

    @Override
    public ReportState startWork(TechnicianReport context) {
        ReportValidationService.validateForWorkStart(context);
        return new InProgressState();
    }
}