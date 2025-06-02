package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public interface ReviewableState extends ReportState {
    ReportState approve(TechnicianReport context);
    ReportState reject(TechnicianReport context);
}
