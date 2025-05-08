package id.ac.ui.cs.advprog.everest.modules.repairorder.exception;

import id.ac.ui.cs.advprog.everest.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TechnicianUnavailableException extends BaseException {

    public TechnicianUnavailableException() {
        super("No technician is available at the moment", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public TechnicianUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}


