package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.RequestStatus;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class IncomingRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IncomingRequestRepository incomingRequestRepository;

    @Test
    public void testSaveIncomingRequest() {
        // Given
        UserRequest userRequest = new UserRequest(null, "Computer not booting");
        entityManager.persist(userRequest);
        entityManager.flush();

        IncomingRequest incomingRequest = IncomingRequest.from(userRequest, 1L);

        // When
        IncomingRequest savedRequest = incomingRequestRepository.save(incomingRequest);

        // Then
        assertNotNull(savedRequest.getRequestId());
        assertEquals(1L, savedRequest.getTechnicianId());
        assertEquals("Computer not booting", savedRequest.getDescription());
        assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
    }

    @Test
    public void testFindByTechnicianId() {
        // Given
        Long technicianId = 1L;

        // Create and persist user requests
        UserRequest userRequest1 = new UserRequest(null, "Request 1");
        UserRequest userRequest2 = new UserRequest(null, "Request 2");
        UserRequest userRequest3 = new UserRequest(null, "Request 3");

        entityManager.persist(userRequest1);
        entityManager.persist(userRequest2);
        entityManager.persist(userRequest3);
        entityManager.flush();

        // Create incoming requests for technician 1
        IncomingRequest incomingRequest1 = IncomingRequest.from(userRequest1, technicianId);
        IncomingRequest incomingRequest2 = IncomingRequest.from(userRequest2, technicianId);

        // Create incoming request for technician 2
        IncomingRequest incomingRequest3 = IncomingRequest.from(userRequest3, 2L);

        entityManager.persist(incomingRequest1);
        entityManager.persist(incomingRequest2);
        entityManager.persist(incomingRequest3);
        entityManager.flush();

        // When
        List<IncomingRequest> technicianRequests = incomingRequestRepository.findByTechnicianId(technicianId);

        // Then
        assertEquals(2, technicianRequests.size());
        assertTrue(technicianRequests.stream().allMatch(request -> request.getTechnicianId().equals(technicianId)));
    }

    @Test
    public void testFindByTechnicianIdAndStatus() {
        // Given
        Long technicianId = 1L;

        // Create and persist user requests
        UserRequest userRequest1 = new UserRequest(null, "Pending Request");
        UserRequest userRequest2 = new UserRequest(null, "Reported Request");

        entityManager.persist(userRequest1);
        entityManager.persist(userRequest2);
        entityManager.flush();

        // Create incoming requests with different statuses
        IncomingRequest pendingRequest = new IncomingRequest(
                userRequest1.getId(), technicianId, userRequest1.getUserDescription(), RequestStatus.PENDING);
        IncomingRequest reportedRequest = new IncomingRequest(
                userRequest2.getId(), technicianId, userRequest2.getUserDescription(), RequestStatus.REPORTED);

        entityManager.persist(pendingRequest);
        entityManager.persist(reportedRequest);
        entityManager.flush();

        // When
        List<IncomingRequest> pendingRequests = incomingRequestRepository
                .findByTechnicianIdAndStatus(technicianId, RequestStatus.PENDING);
        List<IncomingRequest> reportedRequests = incomingRequestRepository
                .findByTechnicianIdAndStatus(technicianId, RequestStatus.REPORTED);

        // Then
        assertEquals(1, pendingRequests.size());
        assertEquals("Pending Request", pendingRequests.get(0).getDescription());
        assertEquals(RequestStatus.PENDING, pendingRequests.get(0).getStatus());

        assertEquals(1, reportedRequests.size());
        assertEquals("Reported Request", reportedRequests.get(0).getDescription());
        assertEquals(RequestStatus.REPORTED, reportedRequests.get(0).getStatus());
    }

    @Test
    public void testFindByRequestIdAndTechnicianId() {
        // Given
        Long technicianId = 1L;

        // Create and persist user request
        UserRequest userRequest = new UserRequest(null, "Test Request");
        entityManager.persist(userRequest);
        entityManager.flush();

        // Create incoming request
        IncomingRequest incomingRequest = IncomingRequest.from(userRequest, technicianId);
        entityManager.persist(incomingRequest);
        entityManager.flush();

        Long requestId = userRequest.getId();

        // When
        Optional<IncomingRequest> foundRequest = incomingRequestRepository
                .findByRequestIdAndTechnicianId(requestId, technicianId);
        Optional<IncomingRequest> notFoundRequest = incomingRequestRepository
                .findByRequestIdAndTechnicianId(requestId, 999L); // Non-existent technician

        // Then
        assertTrue(foundRequest.isPresent());
        assertEquals(requestId, foundRequest.get().getRequestId());
        assertEquals(technicianId, foundRequest.get().getTechnicianId());

        assertFalse(notFoundRequest.isPresent());
    }

    @Test
    public void testUpdateIncomingRequestStatus() {
        // Given
        UserRequest userRequest = new UserRequest(null, "Update status test");
        entityManager.persist(userRequest);
        entityManager.flush();

        IncomingRequest incomingRequest = IncomingRequest.from(userRequest, 1L);
        entityManager.persist(incomingRequest);
        entityManager.flush();

        // When - Create a new instance with updated status since IncomingRequest is immutable
        IncomingRequest updatedRequest = new IncomingRequest(
                incomingRequest.getRequestId(),
                incomingRequest.getTechnicianId(),
                incomingRequest.getDescription(),
                RequestStatus.REPORTED
        );
        incomingRequestRepository.save(updatedRequest);

        // Then
        Optional<IncomingRequest> retrievedRequest = incomingRequestRepository.findById(incomingRequest.getRequestId());
        assertTrue(retrievedRequest.isPresent());
        assertEquals(RequestStatus.REPORTED, retrievedRequest.get().getStatus());
    }
}