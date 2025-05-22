package id.ac.ui.cs.advprog.everest.modules.technicianReport.dto;

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
    private BigDecimal estimatedCost;
    private Long estimatedTimeSeconds;

    public void validate() {
        if (repairOrderId == null || repairOrderId.isBlank()) {
            throw new IllegalArgumentException("repairOrderId cannot be null or blank");
        }
        if (diagnosis == null || diagnosis.isBlank()) {
            throw new IllegalArgumentException("diagnosis cannot be null or blank");
        }
        if (actionPlan == null || actionPlan.isBlank()) {
            throw new IllegalArgumentException("actionPlan cannot be null or blank");
        }
        if (estimatedCost == null || estimatedCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("estimatedCost cannot be null or negative");
        }
        if (estimatedTimeSeconds == null || estimatedTimeSeconds <= 0) {
            throw new IllegalArgumentException("estimatedTimeSeconds must be positive");
        }
    }
}