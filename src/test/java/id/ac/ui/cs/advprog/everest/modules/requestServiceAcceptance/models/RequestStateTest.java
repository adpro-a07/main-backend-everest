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

    @Test
    void testEstimatedStateInitialStatus() {
        RequestState state = new EstimatedState();
        assertEquals(RequestStatus.ESTIMATED, state.getStatus());
    }

    @Test
    void testEstimatedStateTransitionToAccepted() {
        RequestState state = new EstimatedState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        RequestState newState = state.processAction("accept", context);
        assertTrue(newState.getStatus() == RequestStatus.ACCEPTED);
    }

    @Test
    void testEstimatedStateTransitionToRejected() {
        RequestState state = new EstimatedState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);
        RequestState newState = state.processAction("reject", context);
        assertTrue(newState.getStatus() == RequestStatus.REJECTED);
    }

    @Test
    void testAcceptedStateInitialStatus() {
        RequestState state = new AcceptedState();
        assertEquals(RequestStatus.ACCEPTED, state.getStatus());
    }

    @Test
    void testAcceptedStateTransitionToInProgress() {
        RequestState state = new AcceptedState();
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);
        RequestState newState = state.processAction("start_work", context);
        assertTrue(newState.getStatus() == RequestStatus.IN_PROGRESS);
    }

    @Test void testRejectedStateInitialStatus() {
        RequestState state = new RejectedState();
        assertEquals(RequestStatus.REJECTED, state.getStatus());
    }
}