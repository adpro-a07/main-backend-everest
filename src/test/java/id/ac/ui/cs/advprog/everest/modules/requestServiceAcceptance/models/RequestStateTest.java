package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestStateTest {
    @Test
    void testPendingStateInitialStatus() {
        RequestState state = new PendingState();

        assertEquals(RequestStatus.PENDING, state.getStatus());
    }

    @Test
    void testPendingStateTransitionToReported() {
        RequestState state = new PendingState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        RequestState newState = state.processAction("create_report", context);

        assertTrue(newState instanceof ReportedState);
        assertEquals(RequestStatus.REPORTED, newState.getStatus());
    }

    @Test
    void testPendingStateInvalidAction() {
        RequestState state = new PendingState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        RequestState newState = state.processAction("invalid_action", context);

        assertSame(state, newState);
        assertEquals(RequestStatus.PENDING, newState.getStatus());
    }
}