package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IncomingRequestTest {
    @Test
    void testIncomingRequestCreation() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        Long technicianId = 200L;

        // Act
        IncomingRequest request = IncomingRequest.from(userRequest, technicianId);

        // Assert
        assertEquals(100L, request.getRequestId());
        assertEquals(200L, request.getTechnicianId());
        assertEquals("Fix washing machine", request.getDescription());
    }

    @Test
    void testIncomingRequestImplementsTechnicianViewableRequest() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");

        // Act
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);

        // Assert
        assertNotNull(request);
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void testIncomingRequestImmutability() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        IncomingRequest request = IncomingRequest.from(userRequest, 200L);

        // Assert - verify fields are final (compile-time check)
        // This test verifies that the object is effectively immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            // This should throw exception if implemented correctly
            request.withDescription("Changed description");
        });
    }
}