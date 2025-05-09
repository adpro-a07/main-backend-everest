package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Table(name = "technician_reports")
@Getter
@NoArgsConstructor // Required for JPA
public class TechnicianReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "action_plan", length = 500)
    private String actionPlan;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "estimated_time_seconds")
    private Long estimatedTimeSeconds;

    // Constructor handles the Duration conversion
    public TechnicianReport(Long reportId, Long requestId, Long technicianId,
                            String diagnosis, String actionPlan,
                            BigDecimal estimatedCost, Duration estimatedTime) {
        this.reportId = reportId;
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.diagnosis = diagnosis;
        this.actionPlan = actionPlan;
        this.estimatedCost = estimatedCost;
        this.estimatedTimeSeconds = estimatedTime != null ? estimatedTime.getSeconds() : null;
    }

    // Getter for Duration that converts from seconds
    public Duration getEstimatedTime() {
        return estimatedTimeSeconds != null ? Duration.ofSeconds(estimatedTimeSeconds) : null;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long reportId;
        private Long requestId;
        private Long technicianId;
        private String diagnosis;
        private String actionPlan;
        private BigDecimal estimatedCost;
        private Duration estimatedTime;

        public Builder reportId(Long reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder requestId(Long requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder technicianId(Long technicianId) {
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
            return new TechnicianReport(reportId, requestId, technicianId,
                    diagnosis, actionPlan, estimatedCost, estimatedTime);
        }
    }
}