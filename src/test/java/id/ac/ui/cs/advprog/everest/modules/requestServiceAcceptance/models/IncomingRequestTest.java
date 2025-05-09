package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IncomingRequestTest {
    @Test
    void testIncomingRequestCreation() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        Long technicianId = 200L;

        IncomingRequest request = IncomingRequest.from(userRequest, technicianId);

        assertEquals(100L, request.getRequestId());
        assertEquals(200L, request.getTechnicianId());
        assertEquals("Fix washing machine", request.getDescription());
    }

    @Test
    void testIncomingRequestImplementsTechnicianViewableRequest() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");

        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);

        assertNotNull(request);
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void testIncomingRequestImmutability() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        IncomingRequest request = IncomingRequest.from(userRequest, 200L);

        assertThrows(UnsupportedOperationException.class, () -> {
            request.withDescription("Changed description");
        });
    }
}