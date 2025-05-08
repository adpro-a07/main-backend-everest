package id.ac.ui.cs.advprog.everest.modules.repairorder.exception;

import id.ac.ui.cs.advprog.everest.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DatabaseException extends BaseException {
  public DatabaseException(String message, Throwable throwable) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
  }
}
