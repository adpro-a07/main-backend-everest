package id.ac.ui.cs.advprog.everest.modules.technicianReport.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testDatabaseExceptionWithMessage() {
        String errorMessage = "Database connection failed";
        DatabaseException exception = new DatabaseException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testDatabaseExceptionWithMessageAndCause() {
        String errorMessage = "Database error";
        Throwable cause = new RuntimeException("Connection timeout");
        DatabaseException exception = new DatabaseException(errorMessage, cause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInvalidTechnicianReportStateExceptionWithMessage() {
        String errorMessage = "Invalid technician report state";
        InvalidTechnicianReportStateException exception = new InvalidTechnicianReportStateException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInvalidTechnicianReportStateExceptionWithMessageAndCause() {
        String errorMessage = "Technician report in invalid state";
        Throwable cause = new RuntimeException("State transition error");
        InvalidTechnicianReportStateException exception = new InvalidTechnicianReportStateException(errorMessage, cause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testIllegalStateTransitionException() {
        String currentState = "DRAFT";
        String attemptedAction = "complete";
        IllegalStateTransitionException exception = new IllegalStateTransitionException(currentState, attemptedAction);

        String expectedMessage = "Cannot perform complete action while in DRAFT state";
        assertEquals(expectedMessage, exception.getMessage());
    }
}