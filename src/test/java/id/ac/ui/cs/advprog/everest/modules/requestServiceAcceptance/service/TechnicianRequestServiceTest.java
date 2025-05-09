package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TechnicianRequestServiceTest {

    private TechnicianRequestRepository technicianRepository;
    private UserRequestRepository userRequestRepository;
    private TechnicianRequestService service;

    @BeforeEach
    void setUp() {
        technicianRepository = mock(TechnicianRequestRepository.class);
        userRequestRepository = mock(UserRequestRepository.class);
        service = new TechnicianRequestServiceImpl(technicianRepository, userRequestRepository);
    }

    @Test
    void testAssignRequestToTechnician_Success() {
        Long requestId = 1L;
        Long technicianId = 101L;
        UserRequest userRequest = new UserRequest(requestId, "Fix laptop");
        IncomingRequest incomingRequest = IncomingRequest.from(userRequest, technicianId);

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(userRequest));
        when(technicianRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(technicianRepository.save(any(IncomingRequest.class))).thenReturn(incomingRequest);

        IncomingRequest result = service.assignRequestToTechnician(requestId, technicianId);

        assertNotNull(result);
        assertEquals(technicianId, result.getTechnicianId());
        verify(technicianRepository).save(any(IncomingRequest.class));
    }

    @Test
    void testAssignRequestToTechnician_FailsWhenAlreadyAssigned() {
        Long requestId = 1L;
        Long technicianId = 101L;
        IncomingRequest existingAssignment = mock(IncomingRequest.class);

        when(technicianRepository.findByRequestId(requestId)).thenReturn(Optional.of(existingAssignment));

        assertThrows(IllegalStateException.class, () -> service.assignRequestToTechnician(requestId, technicianId));
    }

    @Test
    void testAssignRequestToTechnician_FailsWhenRequestNotFound() {
        Long requestId = 999L;
        Long technicianId = 101L;

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.assignRequestToTechnician(requestId, technicianId));
    }

    @Test
    void testGetRequestById_WhenExists() {
        Long requestId = 1L;
        IncomingRequest request = new IncomingRequest(requestId, 101L, "Fix laptop", null);

        when(technicianRepository.findByRequestId(requestId)).thenReturn(Optional.of(request));

        Optional<IncomingRequest> result = service.getRequestById(requestId);

        assertTrue(result.isPresent());
        assertEquals(101L, result.get().getTechnicianId());
    }

    @Test
    void testGetRequestById_WhenNotExists() {
        when(technicianRepository.findByRequestId(999L)).thenReturn(Optional.empty());

        Optional<IncomingRequest> result = service.getRequestById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetRequestsByTechnician_ReturnsCorrectList() {
        Long technicianId = 101L;
        List<IncomingRequest> requests = List.of(
                new IncomingRequest(1L, technicianId, "Fix laptop", null),
                new IncomingRequest(2L, technicianId, "Replace printer", null)
        );

        when(technicianRepository.findByTechnicianId(technicianId)).thenReturn(requests);

        List<IncomingRequest> result = service.getRequestsByTechnician(technicianId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getTechnicianId().equals(technicianId)));
    }

    @Test
    void testGetAllRequests_ReturnsAllStoredRequests() {
        List<IncomingRequest> requests = List.of(
                new IncomingRequest(1L, 101L, "Fix laptop", null),
                new IncomingRequest(2L, 102L, "Fix printer", null)
        );

        when(technicianRepository.findAll()).thenReturn(requests);

        List<IncomingRequest> result = service.getAllRequests();

        assertEquals(2, result.size());
    }

    @Test
    void testDeleteRequest_WhenExists() {
        Long requestId = 1L;
        when(technicianRepository.deleteByRequestId(requestId)).thenReturn(true);

        boolean deleted = service.deleteRequest(requestId);

        assertTrue(deleted);
    }

    @Test
    void testDeleteRequest_WhenNotExists() {
        when(technicianRepository.deleteByRequestId(999L)).thenReturn(false);

        boolean deleted = service.deleteRequest(999L);

        assertFalse(deleted);
    }
}
