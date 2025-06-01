package id.ac.ui.cs.advprog.everest.modules.technicianreport.factory;

import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.*;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants;
import java.util.Map;
import java.util.function.Supplier;

public class ReportStateFactory {

    private ReportStateFactory() {
        // Private constructor to prevent instantiation
    }

    private static final Map<String, Supplier<ReportState>> STATE_MAP = Map.of(
            ReportConstants.DRAFT, DraftState::new,
            ReportConstants.SUBMITTED, SubmittedState::new,
            ReportConstants.APPROVED, ApprovedState::new,
            ReportConstants.REJECTED, RejectedState::new,
            ReportConstants.IN_PROGRESS, InProgressState::new,
            ReportConstants.COMPLETED, CompletedState::new
    );

    public static ReportState createState(String status) {
        if (status == null) {
            return new DraftState();
        }

        Supplier<ReportState> stateSupplier = STATE_MAP.get(status);
        if (stateSupplier == null) {
            throw new IllegalStateException("Unknown status: " + status);
        }

        return stateSupplier.get();
    }
}