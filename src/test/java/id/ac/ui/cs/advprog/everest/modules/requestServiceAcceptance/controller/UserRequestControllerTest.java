package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidUserRequestStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.UserRequestService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRequestControllerTest {

    private UserRequestService userRequestService;
    private UserRequestController controller;
    private AuthenticatedUser customer;
    private AuthenticatedUser differentCustomer;

    @BeforeEach
    void setUp() {
        userRequestService = mock(UserRequestService.class);
        controller = new UserRequestController(userRequestService);

        UUID customerId = UUID.randomUUID();
        customer = new AuthenticatedUser(
                customerId,
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

        differentCustomer = new AuthenticatedUser(
                UUID.randomUUID(),
                "different@example.com",
                "Different Customer",
                UserRole.CUSTOMER,
                "9876543210",
                Instant.now(),
                Instant.now(),
                "Jakarta",
                null,
                0,
                0L
        );
    }

    @Test
    void whenCreateUserRequest_withValidRequest_shouldReturn201Created() {
        // Arrange
        CreateAndUpdateUserRequest request = new CreateAndUpdateUserRequest();
        request.setUserDescription("Fix my washing machine");

        ViewUserRequestResponse responseDto = ViewUserRequestResponse.builder()
                .requestId(UUID.randomUUID())
                .userId(customer.id())
                .userDescription("Fix my washing machine")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        GenericResponse<ViewUserRequestResponse> expectedResponse = new GenericResponse<>(
                true,
                "User request created successfully",
                responseDto
        );

        when(userRequestService.createUserRequest(request, customer)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.createUserRequest(request, customer);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).createUserRequest(request, customer);
    }

    @Test
    void whenCreateUserRequest_withServiceThrowsException_shouldThrow() {
        // Arrange
        CreateAndUpdateUserRequest request = new CreateAndUpdateUserRequest();
        request.setUserDescription("Bad request");

        when(userRequestService.createUserRequest(request, customer))
                .thenThrow(new InvalidUserRequestStateException("Invalid request"));

        // Act & Assert
        InvalidUserRequestStateException ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.createUserRequest(request, customer));
        assertEquals("Invalid request", ex.getMessage());
        verify(userRequestService).createUserRequest(request, customer);
    }

    @Test
    void whenGetUserRequests_shouldReturnOnlyCustomerRequests() {
        // Arrange
        ViewUserRequestResponse responseDto = ViewUserRequestResponse.builder()
                .requestId(UUID.randomUUID())
                .userId(customer.id())
                .userDescription("Fix my washing machine")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<ViewUserRequestResponse> customerRequests = Collections.singletonList(responseDto);

        GenericResponse<List<ViewUserRequestResponse>> expectedResponse = new GenericResponse<>(
                true,
                "User requests retrieved successfully",
                customerRequests
        );

        when(userRequestService.getUserRequests(customer)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.getUserRequests(customer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).getUserRequests(customer);
    }

    @Test
    void whenGetUserRequests_unauthorized_shouldThrowAccessDenied() {
        // Arrange
        when(userRequestService.getUserRequests(customer))
                .thenThrow(new AccessDeniedException("Forbidden"));

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> controller.getUserRequests(customer));
        assertEquals("Forbidden", ex.getMessage());
        verify(userRequestService).getUserRequests(customer);
    }

    @Test
    void whenGetUserRequestById_shouldReturnRequestIfBelongsToCustomer() {
        // Arrange
        String requestId = UUID.randomUUID().toString();

        ViewUserRequestResponse responseDto = ViewUserRequestResponse.builder()
                .requestId(UUID.fromString(requestId))
                .userId(customer.id())
                .userDescription("Fix my washing machine")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        GenericResponse<ViewUserRequestResponse> expectedResponse = new GenericResponse<>(
                true,
                "User request retrieved successfully",
                responseDto
        );

        when(userRequestService.getUserRequestById(requestId, customer)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.getUserRequestById(requestId, customer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).getUserRequestById(requestId, customer);
    }

    @Test
    void whenGetUserRequestById_requestBelongsToOtherCustomer_shouldThrowException() {
        // Arrange
        String requestId = UUID.randomUUID().toString();

        when(userRequestService.getUserRequestById(requestId, differentCustomer))
                .thenThrow(new InvalidUserRequestStateException("You are not authorized to view this request"));

        // Act & Assert
        InvalidUserRequestStateException ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.getUserRequestById(requestId, differentCustomer));
        assertEquals("You are not authorized to view this request", ex.getMessage());
        verify(userRequestService).getUserRequestById(requestId, differentCustomer);
    }

    @Test
    void whenUpdateUserRequest_shouldReturnUpdatedRequest() {
        // Arrange
        String requestId = UUID.randomUUID().toString();
        CreateAndUpdateUserRequest request = new CreateAndUpdateUserRequest();
        request.setUserDescription("Updated description");

        ViewUserRequestResponse responseDto = ViewUserRequestResponse.builder()
                .requestId(UUID.fromString(requestId))
                .userId(customer.id())
                .userDescription("Updated description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        GenericResponse<ViewUserRequestResponse> expectedResponse = new GenericResponse<>(
                true,
                "User request updated successfully",
                responseDto
        );

        when(userRequestService.updateUserRequest(requestId, request, customer)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.updateUserRequest(requestId, request, customer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).updateUserRequest(requestId, request, customer);
    }

    @Test
    void whenUpdateUserRequest_requestNotFound_shouldThrow() {
        // Arrange
        String requestId = "nonexistent-id";
        CreateAndUpdateUserRequest request = new CreateAndUpdateUserRequest();
        request.setUserDescription("Updated description");

        when(userRequestService.updateUserRequest(requestId, request, customer))
                .thenThrow(new InvalidUserRequestStateException("User request not found"));

        // Act & Assert
        Exception ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.updateUserRequest(requestId, request, customer));
        assertEquals("User request not found", ex.getMessage());
        verify(userRequestService).updateUserRequest(requestId, request, customer);
    }

    @Test
    void whenUpdateUserRequest_notOwnedByCustomer_shouldThrow() {
        // Arrange
        String requestId = UUID.randomUUID().toString();
        CreateAndUpdateUserRequest request = new CreateAndUpdateUserRequest();
        request.setUserDescription("Updated description");

        when(userRequestService.updateUserRequest(requestId, request, differentCustomer))
                .thenThrow(new InvalidUserRequestStateException("You are not authorized to update this request"));

        // Act & Assert
        Exception ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.updateUserRequest(requestId, request, differentCustomer));
        assertEquals("You are not authorized to update this request", ex.getMessage());
        verify(userRequestService).updateUserRequest(requestId, request, differentCustomer);
    }

    @Test
    void whenDeleteUserRequest_shouldReturnSuccess() {
        // Arrange
        String requestId = UUID.randomUUID().toString();

        GenericResponse<Void> expectedResponse = new GenericResponse<>(
                true,
                "User request deleted successfully",
                null
        );

        when(userRequestService.deleteUserRequest(requestId, customer)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = controller.deleteUserRequest(requestId, customer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userRequestService).deleteUserRequest(requestId, customer);
    }

    @Test
    void whenDeleteUserRequest_requestNotFound_shouldThrow() {
        // Arrange
        String requestId = "bad-id";

        when(userRequestService.deleteUserRequest(requestId, customer))
                .thenThrow(new InvalidUserRequestStateException("User request not found"));

        // Act & Assert
        InvalidUserRequestStateException ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.deleteUserRequest(requestId, customer));
        assertEquals("User request not found", ex.getMessage());
        verify(userRequestService).deleteUserRequest(requestId, customer);
    }

    @Test
    void whenDeleteUserRequest_notOwnedByCustomer_shouldThrow() {
        // Arrange
        String requestId = UUID.randomUUID().toString();

        when(userRequestService.deleteUserRequest(requestId, differentCustomer))
                .thenThrow(new InvalidUserRequestStateException("You are not authorized to delete this request"));

        // Act & Assert
        InvalidUserRequestStateException ex = assertThrows(InvalidUserRequestStateException.class,
                () -> controller.deleteUserRequest(requestId, differentCustomer));
        assertEquals("You are not authorized to delete this request", ex.getMessage());
        verify(userRequestService).deleteUserRequest(requestId, differentCustomer);
    }
}