package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class StatusLogTest {
    @Test
    void testStatusLogCreation() {
        // Arrange
        Long requestId = 100L;
        Long technicianId = 200L;
        RequestStatus oldStatus = RequestStatus.PENDING;
        RequestStatus newStatus = RequestStatus.REPORTED;

        // Act
        StatusLog log = new StatusLog(requestId, oldStatus, newStatus, technicianId);

        // Assert
        assertEquals(requestId, log.getRequestId());
        assertEquals(oldStatus, log.getOldStatus());
        assertEquals(newStatus, log.getNewStatus());
        assertEquals(technicianId, log.getTechnicianId());
        assertNotNull(log.getTimestamp());
        assertTrue(log.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(log.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void testStatusLogToString() {
        // Arrange
        StatusLog log = new StatusLog(100L, RequestStatus.PENDING,
                RequestStatus.REPORTED, 200L);

        // Act
        String logString = log.toString();

        // Assert
        assertTrue(logString.contains("PENDING"));
        assertTrue(logString.contains("REPORTED"));
        assertTrue(logString.contains("200"));
    }
}