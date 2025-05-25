package id.ac.ui.cs.advprog.everest.modules.technicianreport.exception;

public class InvalidTechnicianReportStateException extends RuntimeException {

    public InvalidTechnicianReportStateException(String message) {
        super(message);
    }

    public InvalidTechnicianReportStateException(String message, Throwable cause) {
        super(message, cause);
    }
}