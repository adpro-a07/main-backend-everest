package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public interface ReportState {
    String getName();
    boolean technicianCanModify();
    boolean customerCanSee();
    void validateReadPermissions(TechnicianReport context);
}