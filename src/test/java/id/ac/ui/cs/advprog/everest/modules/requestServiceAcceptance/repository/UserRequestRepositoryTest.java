package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRequestRepository userRequestRepository;

    @Test
    public void testSaveUserRequest() {
        UserRequest userRequest = new UserRequest(null, "My computer is not working");

        UserRequest savedRequest = userRequestRepository.save(userRequest);

        assertNotNull(savedRequest.getId());
        assertEquals("My computer is not working", savedRequest.getUserDescription());
    }

    @Test
    public void testFindUserRequestById() {
        UserRequest userRequest = new UserRequest(null, "My internet connection is slow");
        entityManager.persist(userRequest);
        entityManager.flush();

        Optional<UserRequest> foundRequest = userRequestRepository.findById(userRequest.getId());

        assertTrue(foundRequest.isPresent());
        assertEquals("My internet connection is slow", foundRequest.get().getUserDescription());
    }

    @Test
    public void testFindAllUserRequests() {
        UserRequest request1 = new UserRequest(null, "My computer is not turning on");
        UserRequest request2 = new UserRequest(null, "My printer is not working");
        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.flush();

        List<UserRequest> requests = userRequestRepository.findAll();

        assertEquals(2, requests.size());
    }

    @Test
    public void testUpdateUserRequest() {
        UserRequest userRequest = new UserRequest(null, "Original description");
        entityManager.persist(userRequest);
        entityManager.flush();

        UserRequest storedRequest = userRequestRepository.findById(userRequest.getId()).get();
        storedRequest.setUserDescription("Updated description");
        userRequestRepository.save(storedRequest);

        UserRequest updatedRequest = userRequestRepository.findById(userRequest.getId()).get();
        assertEquals("Updated description", updatedRequest.getUserDescription());
    }

    @Test
    public void testDeleteUserRequest() {
        UserRequest userRequest = new UserRequest(null, "Request to be deleted");
        entityManager.persist(userRequest);
        entityManager.flush();

        userRequestRepository.deleteById(userRequest.getId());

        Optional<UserRequest> deletedRequest = userRequestRepository.findById(userRequest.getId());
        assertFalse(deletedRequest.isPresent());
    }
}