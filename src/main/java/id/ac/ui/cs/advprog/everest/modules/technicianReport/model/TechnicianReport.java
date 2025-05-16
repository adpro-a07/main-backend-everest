package id.ac.ui.cs.advprog.everest.modules.technicianReport.model;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.state.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "technician_reports")
@Getter
@Setter
@NoArgsConstructor
public class TechnicianReport {
    @Id
    @Column(name = "report_id", nullable = false, updatable = false)
    private UUID reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private UserRequest userRequest;

    @Column(name = "technician_id", nullable = false)
    private UUID technicianId;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "action_plan", length = 500)
    private String actionPlan;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "estimated_time_seconds")
    private Long estimatedTimeSeconds;

    @Column(name = "status")
    private String status = "DRAFT";


    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Transient
    private ReportState state = new DraftState();

    public TechnicianReport(UUID reportId, UserRequest userRequest, UUID technicianId,
                            String diagnosis, String actionPlan,
                            BigDecimal estimatedCost, Duration estimatedTime) {
        this.reportId = reportId;
        this.userRequest = userRequest;
        this.technicianId = technicianId;
        this.diagnosis = diagnosis;
        this.actionPlan = actionPlan;
        this.estimatedCost = estimatedCost;
        this.estimatedTimeSeconds = estimatedTime != null ? estimatedTime.getSeconds() : null;
        this.status = "DRAFT";
        this.state = new DraftState();
    }

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
    private void initializeState() {
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

    public boolean canEdit() {
        return state.canEdit();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID reportId;
        private UserRequest userRequest;
        private UUID technicianId;
        private String diagnosis;
        private String actionPlan;
        private BigDecimal estimatedCost;
        private Duration estimatedTime;

        public Builder reportId(UUID reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder userRequest(UserRequest userRequest) {
            this.userRequest = userRequest;
            return this;
        }

        public Builder UserRequest(UserRequest userRequest) {
            return userRequest(userRequest);
        }

        public Builder technicianId(UUID technicianId) {
            this.technicianId = technicianId;
            return this;
        }

        public Builder diagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
            return this;
        }

        public Builder actionPlan(String actionPlan) {
            this.actionPlan = actionPlan;
            return this;
        }

        public Builder estimatedCost(BigDecimal estimatedCost) {
            this.estimatedCost = estimatedCost;
            return this;
        }

        public Builder estimatedTime(Duration estimatedTime) {
            this.estimatedTime = estimatedTime;
            return this;
        }

        public TechnicianReport build() {
            return new TechnicianReport(reportId, userRequest, technicianId,
                    diagnosis, actionPlan, estimatedCost, estimatedTime);
        }
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