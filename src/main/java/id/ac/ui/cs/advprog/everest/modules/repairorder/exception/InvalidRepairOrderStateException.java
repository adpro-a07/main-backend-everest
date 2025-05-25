package id.ac.ui.cs.advprog.everest.modules.repairorder.exception;

import id.ac.ui.cs.advprog.everest.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidRepairOrderStateException extends BaseException {
    public InvalidRepairOrderStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidRepairOrderStateException(String message, Throwable throwable) {
        super(message, HttpStatus.BAD_REQUEST, throwable);
    }
}
