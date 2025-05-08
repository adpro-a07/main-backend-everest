package id.ac.ui.cs.advprog.everest.modules.report.model.enums;

public enum ReportStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    COMPLETED,
    IN_PROGRESS;

    public boolean isEmpty() {
        return this == REJECTED || this == COMPLETED;
    }
}
