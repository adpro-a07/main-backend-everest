package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRequestServiceTest {

    private UserRequestRepository repository;
    private UserRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRequestRepository.class);
        service = new UserRequestServiceImpl(repository);
    }

    @Test
    void testCreateRequest_ShouldGenerateIdAndSave() {
        UserRequest savedRequest = new UserRequest(1L, "Fix laptop");

        when(repository.save(any(UserRequest.class))).thenReturn(savedRequest);

        UserRequest result = service.createRequest(savedRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fix laptop", result.getUserDescription());
        verify(repository).save(any(UserRequest.class));
    }

    @Test
    void testGetRequestById_WhenExists() {
        UserRequest request = new UserRequest(1L, "Fix laptop");
        when(repository.findById(1L)).thenReturn(Optional.of(request));

        Optional<UserRequest> result = service.getRequestById(1L);

        assertTrue(result.isPresent());
        assertEquals("Fix laptop", result.get().getUserDescription());
    }

    @Test
    void testGetRequestById_WhenNotExists() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<UserRequest> result = service.getRequestById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllRequests_ReturnsList() {
        List<UserRequest> mockList = List.of(
                new UserRequest(1L, "Fix laptop"),
                new UserRequest(2L, "Fix printer")
        );

        when(repository.findAll()).thenReturn(mockList);

        List<UserRequest> result = service.getAllRequests();

        assertEquals(2, result.size());
    }

    @Test
    void testDeleteRequestById_WhenExists() {
        when(repository.deleteById(1L)).thenReturn(true);

        boolean result = service.deleteRequestById(1L);

        assertTrue(result);
    }

    @Test
    void testDeleteRequestById_WhenNotExists() {
        when(repository.deleteById(999L)).thenReturn(false);

        boolean result = service.deleteRequestById(999L);

        assertFalse(result);
    }

    @Test
    void testCreateWithId_WhenDuplicate_ShouldThrow() {
        UserRequest request = new UserRequest(1L, "Fix laptop");

        when(repository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> {
            service.createWithId(request);
        });
    }

    @Test
    void testCreateWithId_WhenUnique_ShouldSave() {
        UserRequest request = new UserRequest(1L, "Fix laptop");

        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(request)).thenReturn(request);

        UserRequest result = service.createWithId(request);

        assertEquals(1L, result.getId());
        verify(repository).save(request);
    }
}
