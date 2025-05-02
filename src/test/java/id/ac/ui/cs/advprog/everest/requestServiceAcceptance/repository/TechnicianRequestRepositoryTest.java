package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.RequestStatus;
import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianRequestRepositoryTest {

    private TechnicianRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TechnicianRequestRepository();
    }

    @Test
    void testSaveTechnicianRequest() {
        UserRequest userRequest = new UserRequest(1L, "Fix my computer");
        IncomingRequest request = IncomingRequest.from(userRequest, 101L);

        IncomingRequest savedRequest = repository.save(request);

        assertEquals(1L, savedRequest.getRequestId());
        assertEquals(101L, savedRequest.getTechnicianId());
        assertEquals("Fix my computer", savedRequest.getDescription());
        assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
    }

    @Test
    void testSaveTechnicianRequestNull() {
        UserRequest userRequest = new UserRequest(1L, "Fix my computer");
        IncomingRequest request = IncomingRequest.from(userRequest, null);

        assertThrows(Exception.class, () -> {
            repository.save(request);
        });
    }

    @Test
    void testFindTechnicianRequestByRequestId() {
        UserRequest userRequest = new UserRequest(1L, "Fix my computer");
        IncomingRequest request = IncomingRequest.from(userRequest, 101L);
        repository.save(request);

        Optional<IncomingRequest> found = repository.findByRequestId(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getRequestId());
        assertEquals(101L, found.get().getTechnicianId());
    }

    @Test
    void testReturnEmptyWhenFindingNonExistentTechnicianRequest() {
        Optional<IncomingRequest> found = repository.findByRequestId(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllTechnicianRequestsByTechnicianId() {
        UserRequest userRequest1 = new UserRequest(1L, "Fix my computer");
        UserRequest userRequest2 = new UserRequest(2L, "Replace keyboard");
        repository.save(IncomingRequest.from(userRequest1, 101L));
        repository.save(IncomingRequest.from(userRequest2, 101L));
        repository.save(IncomingRequest.from(new UserRequest(3L, "Fix printer"), 102L));

        List<IncomingRequest> requests = repository.findByTechnicianId(101L);

        assertEquals(2, requests.size());
        assertTrue(requests.stream().allMatch(r -> r.getTechnicianId().equals(101L)));
    }

    @Test
    void testFindAllTechnicianRequests() {
        UserRequest userRequest1 = new UserRequest(1L, "Fix my computer");
        UserRequest userRequest2 = new UserRequest(2L, "Replace keyboard");
        repository.save(IncomingRequest.from(userRequest1, 101L));
        repository.save(IncomingRequest.from(userRequest2, 102L));

        List<IncomingRequest> requests = repository.findAll();

        assertEquals(2, requests.size());
    }

    @Test
    void testDeleteTechnicianRequestByRequestId() {
        UserRequest userRequest = new UserRequest(1L, "Fix my computer");
        repository.save(IncomingRequest.from(userRequest, 101L));

        boolean deleted = repository.deleteByRequestId(1L);

        assertTrue(deleted);
        assertTrue(repository.findByRequestId(1L).isEmpty());
    }

    @Test
    void testReturnFalseWhenDeletingNonExistentTechnicianRequest() {
        boolean deleted = repository.deleteByRequestId(999L);

        assertFalse(deleted);
    }

    @Test
    void testAllowOnlyOneTechnicianPerRequest() {
        UserRequest userRequest = new UserRequest(1L, "Fix my computer");
        repository.save(IncomingRequest.from(userRequest, 101L));

        IncomingRequest duplicateAssignment = IncomingRequest.from(userRequest, 102L);

        assertThrows(Exception.class, () -> {
            repository.save(duplicateAssignment);
        });
    }
}