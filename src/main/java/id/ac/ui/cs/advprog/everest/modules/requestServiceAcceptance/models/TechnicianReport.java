package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import java.math.BigDecimal;
import java.time.Duration;

public class TechnicianReport {
    private final Long reportId;
    private final Long requestId;
    private final Long technicianId;
    private final String diagnosis;
    private final String actionPlan;
    private final BigDecimal estimatedCost;
    private final Duration estimatedTime;

    public TechnicianReport(Long reportId, Long requestId, Long technicianId,
                            String diagnosis, String actionPlan,
                            BigDecimal estimatedCost, Duration estimatedTime) {
        this.reportId = reportId;
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.diagnosis = diagnosis;
        this.actionPlan = actionPlan;
        this.estimatedCost = estimatedCost;
        this.estimatedTime = estimatedTime;
    }

    // Getters
    public Long getReportId() { return reportId; }
    public Long getRequestId() { return requestId; }
    public Long getTechnicianId() { return technicianId; }
    public String getDiagnosis() { return diagnosis; }
    public String getActionPlan() { return actionPlan; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public Duration getEstimatedTime() { return estimatedTime; }

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