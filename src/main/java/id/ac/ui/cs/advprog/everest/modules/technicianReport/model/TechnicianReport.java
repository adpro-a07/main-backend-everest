package id.ac.ui.cs.advprog.everest.modules.technicianReport.model;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "technician_reports")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianReport {
    @Id
    @Column(name = "report_id", nullable = false, updatable = false)
    private UUID reportId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repair_orders", nullable = false)
    private RepairOrder repairOrder;

    @Column(name = "technician_id", nullable = false)
    private UUID technicianId;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "action_plan", length = 500)
    private String actionPlan;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private Long estimatedCost;

    @Column(name = "estimated_time_seconds")
    private Long estimatedTimeSeconds;

    @Builder.Default
    @Column(name = "status")
    private String status = "DRAFT";

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder.Default
    @Transient
    private ReportState state = new DraftState();

    public Duration getEstimatedTime() {
        return estimatedTimeSeconds != null ? Duration.ofSeconds(estimatedTimeSeconds) : null;
    }

    public void submit() {
        ReportState newState = state.submit(this);
        updateState(newState);
    }

    public void approve() {
        ReportState newState = state.approve(this);
        updateState(newState);
    }

    public void reject() {
        ReportState newState = state.reject(this);
        updateState(newState);
    }

    public void startWork() {
        ReportState newState = state.startWork(this);
        updateState(newState);
    }

    public void complete() {
        ReportState newState = state.complete(this);
        updateState(newState);
    }

    private void updateState(ReportState newState) {
        this.state = newState;
        this.status = newState.getName();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PostLoad
    void initializeState() {
        if (status == null) {
            this.state = new DraftState();
            return;
        }

        this.state = switch (status) {
            case "DRAFT" -> new DraftState();
            case "SUBMITTED" -> new SubmittedState();
            case "APPROVED" -> new ApprovedState();
            case "REJECTED" -> new RejectedState();
            case "IN_PROGRESS" -> new InProgressState();
            case "COMPLETED" -> new CompletedState();
            default -> throw new IllegalStateException("Unknown status: " + status);
        };
    }

    public boolean technicianCanModify() {
        return state.technicianCanModify();
    }

    public boolean customerCanSee() {
        return state.customerCanSee();
    }

    @PrePersist
    protected void onCreate() {
        if (reportId == null) {
            reportId = UUID.randomUUID();
        }
        if (lastUpdatedAt == null) {
            lastUpdatedAt = LocalDateTime.now();
        }
    }
}