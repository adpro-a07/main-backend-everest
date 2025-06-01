package id.ac.ui.cs.advprog.everest.modules.technicianreport.exception;

public class IllegalAccessTechnicianReport extends RuntimeException {
    public IllegalAccessTechnicianReport(String role, String action) {
        super(role + " not authorized" + action);
    }
}