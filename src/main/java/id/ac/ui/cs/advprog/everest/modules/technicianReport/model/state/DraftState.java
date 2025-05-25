package id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;

public class DraftState extends AbstractReportState {
    @Override
    public String getName() {
        return "DRAFT";
    }

    @Override
    public ReportState submit(TechnicianReport context) {
        if (context.getDiagnosis() == null || context.getDiagnosis().isEmpty()) {
            throw new IllegalStateException("Diagnosis is required before submitting");
        }
        if (context.getEstimatedCost() == null) {
            throw new IllegalStateException("Estimated cost is required before submitting");
        }
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