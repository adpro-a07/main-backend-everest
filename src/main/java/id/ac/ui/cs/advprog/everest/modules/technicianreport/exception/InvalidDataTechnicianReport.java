package id.ac.ui.cs.advprog.everest.modules.technicianreport.exception;

public class InvalidDataTechnicianReport extends RuntimeException {
    public InvalidDataTechnicianReport(String message) {
        super("Technician Report Data Error: " + message);
    }
}