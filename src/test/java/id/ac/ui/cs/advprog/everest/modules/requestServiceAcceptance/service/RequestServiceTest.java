package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.*;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.IncomingRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.StatusLogRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    private UserRequestRepository userRequestRepository;

    @Mock
    private IncomingRequestRepository incomingRequestRepository;

    @Mock
    private StatusLogRepository statusLogRepository;

    @InjectMocks
    private RequestService requestService;

    private UserRequest userRequest;
    private IncomingRequest incomingRequest;
    private StatusLog statusLog;
    private Long requestId = 1L;
    private Long technicianId = 2L;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest(requestId, "Computer not working");
        incomingRequest = IncomingRequest.from(userRequest, technicianId);
        statusLog = new StatusLog(requestId, RequestStatus.PENDING, RequestStatus.REPORTED, technicianId);
    }

    @Test
    void testCreateUserRequest() {
        when(userRequestRepository.save(any(UserRequest.class))).thenReturn(userRequest);

        UserRequest result = requestService.createUserRequest(userRequest);

        assertEquals(userRequest, result);
        verify(userRequestRepository).save(userRequest);
    }

    @Test
    void testAssignToTechnician() {
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(userRequest));
        when(incomingRequestRepository.save(any(IncomingRequest.class))).thenReturn(incomingRequest);

        IncomingRequest result = requestService.assignToTechnician(requestId, technicianId);

        assertEquals(incomingRequest, result);
        verify(userRequestRepository).findById(requestId);
        verify(incomingRequestRepository).save(any(IncomingRequest.class));
    }

    @Test
    void testAssignToTechnicianWithInvalidRequestId() {
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                requestService.assignToTechnician(requestId, technicianId)
        );

        assertEquals("User request not found with id: " + requestId, exception.getMessage());
        verify(userRequestRepository).findById(requestId);
        verify(incomingRequestRepository, never()).save(any(IncomingRequest.class));
    }

    @Test
    void testGetTechnicianRequests() {
        List<IncomingRequest> expectedRequests = Arrays.asList(incomingRequest);
        when(incomingRequestRepository.findByTechnicianId(technicianId)).thenReturn(expectedRequests);

        List<IncomingRequest> result = requestService.getTechnicianRequests(technicianId);

        assertEquals(expectedRequests, result);
        verify(incomingRequestRepository).findByTechnicianId(technicianId);
    }

    @Test
    void testGetTechnicianRequestsByStatus() {
        List<IncomingRequest> expectedRequests = Arrays.asList(incomingRequest);
        when(incomingRequestRepository.findByTechnicianIdAndStatus(technicianId, RequestStatus.PENDING))
                .thenReturn(expectedRequests);

        List<IncomingRequest> result = requestService.getTechnicianRequestsByStatus(technicianId, RequestStatus.PENDING);

        assertEquals(expectedRequests, result);
        verify(incomingRequestRepository).findByTechnicianIdAndStatus(technicianId, RequestStatus.PENDING);
    }

    @Test
    void testProcessRequestAction_CreateReport() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(incomingRequest));
        when(statusLogRepository.findByRequestIdOrderByTimestampDesc(requestId))
                .thenReturn(List.of());
        when(incomingRequestRepository.findById(requestId))
                .thenReturn(Optional.of(incomingRequest));

        requestService.processRequestAction(requestId, technicianId, "create_report");

        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(statusLogRepository).findByRequestIdOrderByTimestampDesc(requestId);
        verify(statusLogRepository).save(any(StatusLog.class));

        RequestContext context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.REPORTED, context.getCurrentStatus());
    }

    @Test
    void testProcessRequestAction_InvalidAction() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(incomingRequest));
        when(statusLogRepository.findByRequestIdOrderByTimestampDesc(requestId))
                .thenReturn(List.of());

        requestService.processRequestAction(requestId, technicianId, "invalid_action");

        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(statusLogRepository).findByRequestIdOrderByTimestampDesc(requestId);
        verify(statusLogRepository, never()).save(any(StatusLog.class));

        RequestContext context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.PENDING, context.getCurrentStatus());
    }

    @Test
    void testProcessRequestAction_RequestNotFound() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                requestService.processRequestAction(requestId, technicianId, "create_report")
        );

        assertEquals("Request not found or not assigned to this technician", exception.getMessage());
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(statusLogRepository, never()).save(any(StatusLog.class));
    }

    @Test
    void testGetRequestContext() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(incomingRequest));
        when(statusLogRepository.findByRequestIdOrderByTimestampDesc(requestId))
                .thenReturn(List.of());

        RequestContext context = requestService.getRequestContext(requestId, technicianId);

        assertNotNull(context);
        assertEquals(RequestStatus.PENDING, context.getCurrentStatus());
        assertEquals(incomingRequest, context.getRequest());
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(statusLogRepository).findByRequestIdOrderByTimestampDesc(requestId);
    }

    @Test
    void testGetRequestContext_NotFound() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                requestService.getRequestContext(requestId, technicianId)
        );

        assertEquals("Request not found or not assigned to this technician", exception.getMessage());
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
    }

    @Test
    void testFullWorkflow() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(incomingRequest));
        when(incomingRequestRepository.findById(requestId))
                .thenReturn(Optional.of(incomingRequest));
        when(statusLogRepository.findByRequestIdOrderByTimestampDesc(requestId))
                .thenReturn(List.of());

        requestService.processRequestAction(requestId, technicianId, "create_report");

        RequestContext context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.REPORTED, context.getCurrentStatus());

        requestService.processRequestAction(requestId, technicianId, "create_estimate");

        context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.ESTIMATED, context.getCurrentStatus());

        requestService.processRequestAction(requestId, technicianId, "accept");

        context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.ACCEPTED, context.getCurrentStatus());

        requestService.processRequestAction(requestId, technicianId, "start_work");

        context = requestService.getRequestContext(requestId, technicianId);
        assertEquals(RequestStatus.IN_PROGRESS, context.getCurrentStatus());

        verify(statusLogRepository, times(4)).save(any(StatusLog.class));
    }
}