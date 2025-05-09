package id.ac.ui.cs.advprog.everest.modules.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReportResponse {
    private UUID id;
    private String technicianName;
    private String repairDetails;
    private LocalDate repairDate;
    private String status;
}
