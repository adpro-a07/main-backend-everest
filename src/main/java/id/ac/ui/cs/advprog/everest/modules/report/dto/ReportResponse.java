package id.ac.ui.cs.advprog.everest.modules.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private String technicianName;
    private String repairDetails;
    private LocalDate repairDate;
    private String status;
}
