package id.ac.ui.cs.advprog.everest.modules.technicianReport.dto;


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
    private UUID repairOrderId;
    private UUID technicianId;
    private String diagnosis;
    private String actionPlan;
    private Long estimatedCost;
    private Long estimatedTimeSeconds;
    private String status;
}