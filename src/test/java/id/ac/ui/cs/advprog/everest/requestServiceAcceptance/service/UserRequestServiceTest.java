package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserRequestServiceTest {

    private UserRequestService service;
    private UserRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(UserRequestRepository.class);
        service = new UserRequestService(repository);
    }

    @Test
    void testCreateRequestSuccessfully() {
        UserRequest input = new UserRequest(null, "Fix my laptop");
        UserRequest saved = new UserRequest(1L, "Fix my laptop");

        when(repository.save(any())).thenReturn(saved);

        UserRequest result = service.createRequest(input);

        assertNotNull(result.getId());
        assertEquals("Fix my laptop", result.getUserDescription());
        verify(repository).save(any());
    }

    @Test
    void testCreateRequestWithEmptyDescription() {
        UserRequest input = new UserRequest(null, "");

        assertThrows(IllegalArgumentException.class, () -> service.createRequest(input));
    }

    @Test
    void testCreateRequestWithDuplicateId() {
        UserRequest input = new UserRequest(1L, "Fix my laptop");

        when(repository.findById(1L)).thenReturn(Optional.of(input));

        assertThrows(IllegalArgumentException.class, () -> service.createRequest(input));
    }
}
