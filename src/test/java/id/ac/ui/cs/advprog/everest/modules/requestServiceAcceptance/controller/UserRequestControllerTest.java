package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.UserRequestService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRequestControllerTest {

    private UserRequestService userRequestService;
    private UserRequestController controller;
    private AuthenticatedUser user;

    @BeforeEach
    void setUp() {
        userRequestService = mock(UserRequestService.class);
        controller = new UserRequestController(userRequestService);
        user = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@example.com",
                "Customer",
                UserRole.CUSTOMER,
                "12301894239",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );
    }

    @Test
    void whenCreateUserRequest_withValidRequest_shouldReturn201Created() {
        CreateAndUpdateUserRequestDto request = new CreateAndUpdateUserRequestDto();
        request.setUserDescription("My device is not working");

        ViewUserRequestDto viewResponse = ViewUserRequestDto.builder()
                .id(1L)
                .userDescription("My device is not working")
                .build();

        GenericResponse<ViewUserRequestDto> expectedResponse = new GenericResponse<>(
                true,
                "User request created successfully",
                viewResponse
        );

        when(userRequestService.createUserRequest(request, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.createUserRequest(request, user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).createUserRequest(request, user);
    }

    @Test
    void whenCreateUserRequest_withServiceThrowsException_shouldThrow() {
        CreateAndUpdateUserRequestDto request = new CreateAndUpdateUserRequestDto();
        when(userRequestService.createUserRequest(request, user))
                .thenThrow(new RuntimeException("Service failure"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.createUserRequest(request, user));
        assertEquals("Service failure", ex.getMessage());
        verify(userRequestService).createUserRequest(request, user);
    }

    @Test
    void whenGetUserRequests_shouldReturnList() {
        List<ViewUserRequestDto> requestList = new ArrayList<>();
        requestList.add(ViewUserRequestDto.builder().id(1L).userDescription("Request 1").build());
        requestList.add(ViewUserRequestDto.builder().id(2L).userDescription("Request 2").build());

        GenericResponse<List<ViewUserRequestDto>> expectedResponse = new GenericResponse<>(
                true,
                "User requests retrieved successfully",
                requestList
        );

        when(userRequestService.getUserRequests(user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.getUserRequests(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).getUserRequests(user);
    }

    @Test
    void whenGetUserRequestById_withValidId_shouldReturnUserRequest() {
        Long requestId = 1L;
        ViewUserRequestDto dto = ViewUserRequestDto.builder()
                .id(requestId)
                .userDescription("Test request")
                .build();

        GenericResponse<ViewUserRequestDto> expectedResponse = new GenericResponse<>(
                true,
                "User request retrieved successfully",
                dto
        );

        when(userRequestService.getUserRequestById(requestId.toString(), user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.getUserRequestById(requestId.toString(), user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).getUserRequestById(requestId.toString(), user);
    }

    @Test
    void whenGetUserRequestById_notFound_shouldThrow() {
        String requestId = "999";
        when(userRequestService.getUserRequestById(requestId, user))
                .thenThrow(new IllegalArgumentException("User request not found"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.getUserRequestById(requestId, user));
        assertEquals("User request not found", ex.getMessage());
        verify(userRequestService).getUserRequestById(requestId, user);
    }

    @Test
    void whenUpdateUserRequest_shouldReturnUpdatedRequest() {
        // Arrange
        String requestId = "1";
        CreateAndUpdateUserRequestDto request = new CreateAndUpdateUserRequestDto();
        request.setUserDescription("Updated description");

        ViewUserRequestDto dto = ViewUserRequestDto.builder()
                .id(1L)
                .userDescription("Updated description")
                .build();

        GenericResponse<ViewUserRequestDto> expectedResponse = new GenericResponse<>(
                true,
                "User request updated successfully",
                dto
        );

        when(userRequestService.updateUserRequest(requestId, request, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.updateUserRequest(requestId, request, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).updateUserRequest(requestId, request, user);
    }

    @Test
    void whenUpdateUserRequest_notFound_shouldThrow() {
        String requestId = "999";
        CreateAndUpdateUserRequestDto request = new CreateAndUpdateUserRequestDto();

        when(userRequestService.updateUserRequest(requestId, request, user))
                .thenThrow(new IllegalArgumentException("User request not found"));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateUserRequest(requestId, request, user));
        assertEquals("User request not found", ex.getMessage());
        verify(userRequestService).updateUserRequest(requestId, request, user);
    }

    @Test
    void whenDeleteUserRequest_shouldReturnSuccess() {
        String requestId = "1";
        GenericResponse<Void> expectedResponse = new GenericResponse<>(
                true,
                "User request deleted successfully",
                null
        );

        when(userRequestService.deleteUserRequest(requestId, user)).thenReturn(expectedResponse);

        ResponseEntity<?> response = controller.deleteUserRequest(requestId, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).deleteUserRequest(requestId, user);
    }

    @Test
    void whenDeleteUserRequest_notFound_shouldThrow() {
        String requestId = "999";

        when(userRequestService.deleteUserRequest(requestId, user))
                .thenThrow(new IllegalArgumentException("User request not found"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.deleteUserRequest(requestId, user));
        assertEquals("User request not found", ex.getMessage());
        verify(userRequestService).deleteUserRequest(requestId, user);
    }

    @Test
    void whenDeleteUserRequest_unauthorized_shouldThrowAccessDenied() {
        String requestId = "1";

        when(userRequestService.deleteUserRequest(requestId, user))
                .thenThrow(new AccessDeniedException("Not allowed"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> controller.deleteUserRequest(requestId, user));
        assertEquals("Not allowed", ex.getMessage());
        verify(userRequestService).deleteUserRequest(requestId, user);
    }
}