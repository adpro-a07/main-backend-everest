package id.ac.ui.cs.advprog.everest.modules.technicianreport.service;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportValidationServiceTest {
    @Test
    void testValidateForSubmission_HappyPath() {
        TechnicianReport report = new TechnicianReport();
        report.setDiagnosis("Diagnosis");
        report.setEstimatedCost(1000L);
        report.setActionPlan("Action plan");

        assertDoesNotThrow(() -> ReportValidationService.validateForSubmission(report));
    }

    @Test
    void testValidateForSubmission_UnhappyPath_MissingDiagnosis() {
        TechnicianReport report = new TechnicianReport();
        report.setDiagnosis(null);
        report.setEstimatedCost(1000L);
        report.setActionPlan("Action plan");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            ReportValidationService.validateForSubmission(report);
        });
    }

    @Test
    void testValidateForSubmission_UnhappyPath_MissingEstimatedCost() {
        TechnicianReport report = new TechnicianReport();
        report.setDiagnosis("Diagnosis");
        report.setEstimatedCost(null);
        report.setActionPlan("Action plan");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            ReportValidationService.validateForSubmission(report);
        });
    }

    @Test
    void testValidateForSubmission_UnhappyPath_MissingActionPlan() {
        TechnicianReport report = new TechnicianReport();
        report.setDiagnosis("Diagnosis");
        report.setEstimatedCost(1000L);
        report.setActionPlan(null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            ReportValidationService.validateForSubmission(report);
        });
    }
}
