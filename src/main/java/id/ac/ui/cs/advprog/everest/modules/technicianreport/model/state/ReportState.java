package id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public interface ReportState {
    String getName();
    ReportState submit(TechnicianReport context);
    ReportState approve(TechnicianReport context);
    ReportState reject(TechnicianReport context);
    ReportState startWork(TechnicianReport context);
    ReportState complete(TechnicianReport context);
    boolean technicianCanModify();
    boolean customerCanSee();

    void readPermissions(TechnicianReport context);
}