package id.ac.ui.cs.advprog.everest.modules.technicianReport.exception;

public class InvalidDataTechnicianReport extends RuntimeException {
    public InvalidDataTechnicianReport(String message) {
        super("Something Missing: " + message);
    }
}
