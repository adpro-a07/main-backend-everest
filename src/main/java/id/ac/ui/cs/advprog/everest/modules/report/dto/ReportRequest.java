package id.ac.ui.cs.advprog.everest.modules.report.dto;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    @NotBlank(message = "Technician name is required")
    @Size(max = 100, message = "Technician name must be less than 100 characters")
    private String technicianName;

    @NotBlank(message = "Repair details are required")
    @Size(max = 1000, message = "Repair details must be less than 1000 characters")
    private String repairDetails;

    @NotNull(message = "Repair date is required")
    private LocalDate repairDate;

    @NotNull(message = "Status is required")
    private ReportStatus status;
}