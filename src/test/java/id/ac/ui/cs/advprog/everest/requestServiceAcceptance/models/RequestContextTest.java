package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {
    @Test
    void testRequestContextInitialState() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);

        // Act
        RequestContext context = new RequestContext(request);

        // Assert
        assertEquals(RequestStatus.PENDING, context.getCurrentStatus());
        assertNull(context.getLastStatusLog());
    }

    @Test
    void testRequestContextProcessValidAction() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        // Act
        context.processAction("create_report");

        // Assert
        assertEquals(RequestStatus.REPORTED, context.getCurrentStatus());
    }

    @Test
    void testRequestContextLogStatusChange() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        // Act
        context.processAction("create_report");
        StatusLog log = context.getLastStatusLog();

        // Assert
        assertNotNull(log);
        assertEquals(RequestStatus.PENDING, log.getOldStatus());
        assertEquals(RequestStatus.REPORTED, log.getNewStatus());
        assertEquals(200L, log.getTechnicianId());
        assertEquals(100L, log.getRequestId());
    }

    @Test
    void testMultipleStatusTransitions() {
        // Arrange
        UserRequest userRequest = new UserRequest(100L, "Fix washing machine");
        TechnicianViewableRequest request = IncomingRequest.from(userRequest, 200L);
        RequestContext context = new RequestContext(request);

        // Act
        context.processAction("create_report");
        context.processAction("create_estimate");

        // Assert
        assertEquals(RequestStatus.ESTIMATED, context.getCurrentStatus());
        assertEquals(2, context.getStatusLogs().size());
    }
}