package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestStateTest {
    @Test
    void testPendingStateInitialStatus() {
        // Arrange
        RequestState state = new PendingState();

        // Act & Assert
        assertEquals(RequestStatus.PENDING, state.getStatus());
    }

    @Test
    void testPendingStateTransitionToReported() {
        // Arrange
        RequestState state = new PendingState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContextTest context = new RequestContextTest(request);

        // Act
        RequestState newState = state.processAction("create_report", context);

        // Assert
        assertTrue(newState instanceof ReportedState);
        assertEquals(RequestStatus.REPORTED, newState.getStatus());
    }

    @Test
    void testPendingStateInvalidAction() {
        // Arrange
        RequestState state = new PendingState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContextTest context = new RequestContextTest(request);

        // Act
        RequestState newState = state.processAction("invalid_action", context);

        // Assert
        assertSame(state, newState);
        assertEquals(RequestStatus.PENDING, newState.getStatus());
    }
}