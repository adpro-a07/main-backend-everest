package id.ac.ui.cs.advprog.everest.modules.report.model;

import java.time.LocalDate;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Report {
    @NonNull
    private int id;

    @NonNull
    private String technicianName;

    @NonNull
    private String repairDetails;

    @NonNull
    private LocalDate repairDate;

    @NonNull
    private ReportStatus status;

    @Builder
    public Report(String technicianName, String repairDetails, LocalDate repairDate, ReportStatus status) {
        this.technicianName = technicianName;
        this.repairDetails = repairDetails;
        this.repairDate = repairDate;
        this.status = status;
    }
}
