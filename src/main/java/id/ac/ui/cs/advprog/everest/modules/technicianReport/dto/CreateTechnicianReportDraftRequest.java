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
}
