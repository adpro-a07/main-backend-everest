// State interface
package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.state;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

public interface ReportState {
    String getName();
    ReportState submit(TechnicianReport context);
    ReportState approve(TechnicianReport context);
    ReportState reject(TechnicianReport context);
    ReportState revise(TechnicianReport context);
    ReportState startWork(TechnicianReport context);
    ReportState complete(TechnicianReport context);
    boolean canEdit();
}