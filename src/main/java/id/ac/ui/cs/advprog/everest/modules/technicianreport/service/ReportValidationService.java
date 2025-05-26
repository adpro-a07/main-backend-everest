package id.ac.ui.cs.advprog.everest.modules.technicianreport.service;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;

public class ReportValidationService {

    public static void validateForSubmission(TechnicianReport report) {
        if (report.getDiagnosis() == null || report.getDiagnosis().trim().isEmpty()) {
            throw new IllegalStateException("Diagnosis is required before submitting");
        }
        if (report.getEstimatedCost() == null) {
            throw new IllegalStateException("Estimated cost is required before submitting");
        }
        if (report.getActionPlan() == null || report.getActionPlan().trim().isEmpty()) {
            throw new IllegalStateException("Action plan is required before submitting");
        }
    }

    public static void validateForApproval(TechnicianReport report) {
        validateForSubmission(report);
    }

    public static void validateForWorkStart(TechnicianReport report) {
        if (report.getEstimatedTimeSeconds() == null) {
            throw new IllegalStateException("Estimated time is required before starting work");
        }
    }
}
