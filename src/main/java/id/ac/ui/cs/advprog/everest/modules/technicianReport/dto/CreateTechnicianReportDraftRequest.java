package id.ac.ui.cs.advprog.everest.modules.technicianReport.dto;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.InvalidDataTechnicianReport;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class CreateTechnicianReportDraftRequest {
    private String repairOrderId;
    private String diagnosis;
    private String actionPlan;
    private Long estimatedCost;
    private Long estimatedTimeSeconds;


    public void validate() {
        if (repairOrderId == null || repairOrderId.isBlank()) {
            throw new InvalidDataTechnicianReport("repairOrderId cannot be null or blank");
        }
        if (diagnosis == null || diagnosis.isBlank()) {
            throw new InvalidDataTechnicianReport("diagnosis cannot be null or blank");
        }
        if (actionPlan == null || actionPlan.isBlank()) {
            throw new InvalidDataTechnicianReport("actionPlan cannot be null or blank");
        }
        if (estimatedCost == null || estimatedCost < 0) {
            throw new InvalidDataTechnicianReport("estimatedCost cannot be null or negative");
        }
        if (estimatedTimeSeconds == null || estimatedTimeSeconds <= 0) {
            throw new InvalidDataTechnicianReport("estimatedTimeSeconds must be positive");
        }
    }
}