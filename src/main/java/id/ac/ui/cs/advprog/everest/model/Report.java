package id.ac.ui.cs.advprog.everest.model;

import java.time.LocalDate;

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
    private Long id;

    @NonNull
    private String technicianName;

    @NonNull
    private String detailPengerjaan;

    @NonNull
    private LocalDate tanggalPengerjaan;

    @NonNull
    private String status;

    @Builder
    public Report(String technicianName, String detailPengerjaan, LocalDate tanggalPengerjaan, String status) {
        this.technicianName = technicianName;
        this.detailPengerjaan = detailPengerjaan;
        this.tanggalPengerjaan = tanggalPengerjaan;
        this.status = status;
    }
}
