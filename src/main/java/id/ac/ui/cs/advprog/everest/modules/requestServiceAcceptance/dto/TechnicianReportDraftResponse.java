package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
public class TechnicianReportDraftResponse {
    private UUID reportId;
    private UUID userRequestId;
    private UUID technicianId;
    private String diagnosis;
    private String actionPlan;
    private BigDecimal estimatedCost;
    private Long estimatedTimeSeconds;
    private String status;

    public TechnicianReportDraftResponse(UUID reportId, UUID userRequestId, UUID technicianId,
                                     String diagnosis, String actionPlan,
                                     BigDecimal estimatedCost, Long estimatedTimeSeconds, String status) {
        this.reportId = reportId;
        this.userRequestId = userRequestId;
        this.technicianId = technicianId;
        this.diagnosis = diagnosis;
        this.actionPlan = actionPlan;
        this.estimatedCost = estimatedCost;
        this.estimatedTimeSeconds = estimatedTimeSeconds;
        this.status = status;
    }
}
