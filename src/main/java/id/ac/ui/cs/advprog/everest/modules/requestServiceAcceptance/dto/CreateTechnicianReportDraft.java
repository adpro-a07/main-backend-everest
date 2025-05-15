package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTechnicianReportDraft {
    private String technicianId;
    private String userRequestId;
    private String diagnosis;
    private String actionPlan;
    private BigDecimal estimatedCost;
    private Long estimatedTimeSeconds;
}
