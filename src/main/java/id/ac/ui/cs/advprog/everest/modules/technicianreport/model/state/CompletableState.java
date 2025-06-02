package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public interface CompletableState extends ReportState {
    ReportState complete(TechnicianReport context);
}
