package id.ac.ui.cs.advprog.everest.modules.report.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String technicianName;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String repairDetails;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate repairDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
