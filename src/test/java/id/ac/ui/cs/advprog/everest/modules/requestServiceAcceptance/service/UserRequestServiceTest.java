package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserRequestServiceTest {

    @Mock
    private UserRequestRepository userRequestRepository;

    @InjectMocks
    private UserRequestServiceImpl userRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUserRequest() {
        // Given
        CreateAndUpdateUserRequestDto dto = new CreateAndUpdateUserRequestDto();
        dto.setUserDescription("Test Description");

        UserRequest userRequest = new UserRequest();
        userRequest.setId(1L);
        userRequest.setUserDescription("Test Description");

        when(userRequestRepository.save(any(UserRequest.class))).thenReturn(userRequest);

        // When
        ViewUserRequestDto result = userRequestService.createUserRequest(dto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Description", result.getUserDescription());
        verify(userRequestRepository, times(1)).save(any(UserRequest.class));
    }

    @Test
    void testGetAllUserRequests() {
        // Given
        UserRequest request1 = new UserRequest(1L, "First Request");
        UserRequest request2 = new UserRequest(2L, "Second Request");
        List<UserRequest> userRequests = Arrays.asList(request1, request2);

        when(userRequestRepository.findAll()).thenReturn(userRequests);

        // When
        List<ViewUserRequestDto> result = userRequestService.getAllUserRequests();

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("First Request", result.get(0).getUserDescription());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Second Request", result.get(1).getUserDescription());
        verify(userRequestRepository, times(1)).findAll();
    }

    @Test
    void testGetUserRequestById_Found() {
        // Given
        UserRequest request = new UserRequest(1L, "Test Request");
        when(userRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        // When
        Optional<ViewUserRequestDto> result = userRequestService.getUserRequestById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Request", result.get().getUserDescription());
        verify(userRequestRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserRequestById_NotFound() {
        // Given
        when(userRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        Optional<ViewUserRequestDto> result = userRequestService.getUserRequestById(999L);

        // Then
        assertTrue(result.isEmpty());
        verify(userRequestRepository, times(1)).findById(999L);
    }

    @Test
    void testUpdateUserRequest_Found() {
        // Given
        UserRequest existingRequest = new UserRequest(1L, "Original Description");
        UserRequest updatedRequest = new UserRequest(1L, "Updated Description");

        CreateAndUpdateUserRequestDto dto = new CreateAndUpdateUserRequestDto();
        dto.setUserDescription("Updated Description");

        when(userRequestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRequestRepository.save(any(UserRequest.class))).thenReturn(updatedRequest);

        // When
        Optional<ViewUserRequestDto> result = userRequestService.updateUserRequest(1L, dto);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Updated Description", result.get().getUserDescription());
        verify(userRequestRepository, times(1)).findById(1L);
        verify(userRequestRepository, times(1)).save(any(UserRequest.class));
    }

    @Test
    void testUpdateUserRequest_NotFound() {
        // Given
        CreateAndUpdateUserRequestDto dto = new CreateAndUpdateUserRequestDto();
        dto.setUserDescription("Updated Description");

        when(userRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ViewUserRequestDto> result = userRequestService.updateUserRequest(999L, dto);

        // Then
        assertTrue(result.isEmpty());
        verify(userRequestRepository, times(1)).findById(999L);
        verify(userRequestRepository, never()).save(any(UserRequest.class));
    }

    @Test
    void testDeleteUserRequest_Found() {
        // Given
        when(userRequestRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRequestRepository).deleteById(1L);

        // When
        boolean result = userRequestService.deleteUserRequest(1L);

        // Then
        assertTrue(result);
        verify(userRequestRepository, times(1)).existsById(1L);
        verify(userRequestRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUserRequest_NotFound() {
        // Given
        when(userRequestRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = userRequestService.deleteUserRequest(999L);

        // Then
        assertFalse(result);
        verify(userRequestRepository, times(1)).existsById(999L);
        verify(userRequestRepository, never()).deleteById(999L);
    }
}