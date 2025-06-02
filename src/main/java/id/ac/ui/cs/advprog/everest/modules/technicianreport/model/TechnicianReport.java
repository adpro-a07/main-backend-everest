package id.ac.ui.cs.advprog.everest.modules.technicianreport.model;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.*;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "diagnosis", length = ReportConstants.MAX_DIAGNOSIS_LENGTH)
    private String diagnosis;

    @Column(name = "action_plan", length = ReportConstants.MAX_ACTION_PLAN_LENGTH)
    private String actionPlan;

    @Column(name = "estimated_cost", precision = ReportConstants.COST_PRECISION, scale = ReportConstants.COST_SCALE)
    private Long estimatedCost;

    @Column(name = "estimated_time_seconds")
    private Long estimatedTimeSeconds;

    @Builder.Default
    @Column(name = "status")
    private String status = ReportConstants.DRAFT;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder.Default
    @Transient
    private ReportState state = new DraftState();

    public Duration getEstimatedTime() {
        return estimatedTimeSeconds != null ? Duration.ofSeconds(estimatedTimeSeconds) : null;
    }

    public void submit() {
        if (!(state instanceof SubmittableState submittableState)) {
            throw new IllegalStateTransitionException("submit", state.getName());
        }
        ReportState newState = submittableState.submit(this);
        updateState(newState);
    }

    public void approve() {
        if (!(state instanceof ReviewableState reviewableState)) {
            throw new IllegalStateTransitionException("approve", state.getName());
        }
        ReportState newState = reviewableState.approve(this);
        updateState(newState);
    }

    public void reject() {
        if (!(state instanceof ReviewableState reviewableState)) {
            throw new IllegalStateTransitionException("reject", state.getName());
        }
        ReportState newState = reviewableState.reject(this);
        updateState(newState);
    }

    public void startWork() {
        if (!(state instanceof WorkableState workableState)) {
            throw new IllegalStateTransitionException("start work", state.getName());
        }
        ReportState newState = workableState.startWork(this);
        updateState(newState);
    }

    public void complete() {
        if (!(state instanceof CompletableState completableState)) {
            throw new IllegalStateTransitionException("complete", state.getName());
        }
        ReportState newState = completableState.complete(this);
        updateState(newState);
    }

    private void updateState(ReportState newState) {
        this.state = newState;
        this.status = newState.getName();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PostLoad
    void initializeState() {
        this.state = ReportStateMapper.createState(status);
    }

    public boolean technicianCanModify() {
        return state.technicianCanModify();
    }

    public boolean customerCanSee() {
        return state.customerCanSee();
    }

    public void validateReadPermissions() {
        state.validateReadPermissions(this);
    }

    @PrePersist
    protected void onCreate() {
        if (reportId == null) {
            reportId = UUID.randomUUID();
        }
        if (lastUpdatedAt == null) {
            lastUpdatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReportConstants.DRAFT;
        }
    }
}