package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {
    @Test
    void testRequestContextInitialState() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);

        RequestContext context = new RequestContext(request);

        assertEquals(RequestStatus.PENDING, context.getCurrentStatus());
        assertNull(context.getLastStatusLog());
    }

    @Test
    void testRequestContextProcessValidAction() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        context.processAction("create_report");

        assertEquals(RequestStatus.REPORTED, context.getCurrentStatus());
    }

    @Test
    void testRequestContextLogStatusChange() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        context.processAction("create_report");
        StatusLog log = context.getLastStatusLog();

        assertNotNull(log);
        assertEquals(RequestStatus.PENDING, log.getOldStatus());
        assertEquals(RequestStatus.REPORTED, log.getNewStatus());
        assertEquals(200L, log.getTechnicianId());
        assertEquals(100L, log.getRequestId());
    }

    @Test
    void testMultipleStatusTransitions() {
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        context.processAction("create_report");
        context.processAction("create_estimate");

        assertEquals(RequestStatus.ESTIMATED, context.getCurrentStatus());
        assertEquals(2, context.getStatusLogs().size());
    }
}