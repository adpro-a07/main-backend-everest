package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestRepositoryTest {

    private UserRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRequestRepository();
    }

    @Test
    void testSaveNewRequest() {
        UserRequest request = new UserRequest(1L, "Fix my laptop");

        UserRequest savedRequest = repository.save(request);

        assertNotNull(savedRequest.getId());
        assertEquals("Fix my laptop", savedRequest.getUserDescription());
    }

    @Test
    void testSaveNewRequestNull() {
        UserRequest request = new UserRequest(null, "Fix my laptop");

        UserRequest savedRequest = repository.save(request);

        assertNotNull(savedRequest.getId());
        assertEquals("Fix my laptop", savedRequest.getUserDescription());
    }

    @Test
    void testFindById_Existing() {
        UserRequest request = repository.save(new UserRequest(null, "Fix my laptop"));

        Optional<UserRequest> found = repository.findById(request.getId());

        assertTrue(found.isPresent());
        assertEquals(request.getId(), found.get().getId());
        assertEquals("Fix my laptop", found.get().getUserDescription());
    }

    @Test
    void testFindById_NonExisting() {
        Optional<UserRequest> found = repository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_Empty() {
        List<UserRequest> all = repository.findAll();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAll_MultipleItems() {
        repository.save(new UserRequest(1L, "Fix my laptop"));
        repository.save(new UserRequest(2L, "Fix my printer"));

        List<UserRequest> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById_Existing() {
        UserRequest request = repository.save(new UserRequest(1L, "Fix my laptop"));

        boolean result = repository.deleteById(request.getId());

        assertTrue(result);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById_NonExisting() {
        boolean result = repository.deleteById(999L);

        assertFalse(result);
    }
}