package id.ac.ui.cs.advprog.everest.modules.technicianreport.constants;

public final class ReportConstants {
    public static final String DRAFT = "DRAFT";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";

    public static final int MAX_DIAGNOSIS_LENGTH = 500;
    public static final int MAX_ACTION_PLAN_LENGTH = 500;
    public static final int COST_PRECISION = 10;
    public static final int COST_SCALE = 2;

    private ReportConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
