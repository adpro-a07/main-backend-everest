package id.ac.ui.cs.advprog.everest.modules.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReportResponse {
    private UUID id;
    private UUID technicianId;
    private String diagnosis;
    private String actionPlan;
    private Long estimatedCost;
    private Long estimatedTimeSeconds;
    private String status;
    private LocalDateTime lastUpdatedAt;
}