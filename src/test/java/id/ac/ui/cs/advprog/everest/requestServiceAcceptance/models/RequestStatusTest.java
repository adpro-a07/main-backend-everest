package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestStatusTest {
    @Test
    void testRequestStatusValues() {
        assertEquals("PENDING", RequestStatus.PENDING.name());
        assertEquals("REPORTED", RequestStatus.REPORTED.name());
        assertEquals("ESTIMATED", RequestStatus.ESTIMATED.name());
        assertEquals("ACCEPTED", RequestStatus.ACCEPTED.name());
        assertEquals("REJECTED", RequestStatus.REJECTED.name());
        assertEquals("IN_PROGRESS", RequestStatus.IN_PROGRESS.name());
    }

    @Test
    void testRequestStatusDisplayName() {
        assertEquals("Menunggu", RequestStatus.PENDING.getDisplayName());
        assertEquals("Dilaporkan", RequestStatus.REPORTED.getDisplayName());
    }
}