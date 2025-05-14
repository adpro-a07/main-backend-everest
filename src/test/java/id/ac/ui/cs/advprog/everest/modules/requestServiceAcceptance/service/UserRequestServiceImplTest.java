package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidUserRequestStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRequestServiceImplTest {

    @Mock
    private UserRequestRepository userRequestRepository;

    @InjectMocks
    private UserRequestServiceImpl userRequestService;

    private UUID customerId;
    private AuthenticatedUser customer;
    private CreateAndUpdateUserRequest validRequest;
    private UserRequest sampleUserRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
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

        validRequest = new CreateAndUpdateUserRequest();
        validRequest.setUserDescription("Fix my washing machine");

        // Sample user request
        sampleUserRequest = new UserRequest();
        sampleUserRequest.setRequestId(UUID.randomUUID());
        sampleUserRequest.setUserId(customerId);
        sampleUserRequest.setUserDescription("Fix my washing machine");
    }

    // CREATE USER REQUEST TESTS

    @Test
    void createUserRequest_Success() {
        // Arrange
        when(userRequestRepository.save(any(UserRequest.class))).thenReturn(sampleUserRequest);

        // Act
        GenericResponse<ViewUserRequestResponse> response = userRequestService.createUserRequest(validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User request created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleUserRequest.getRequestId(), response.getData().getRequestId());
        assertEquals(customerId, response.getData().getUserId());
        assertEquals(validRequest.getUserDescription(), response.getData().getUserDescription());

        verify(userRequestRepository).save(any(UserRequest.class));
    }

    @Test
    void createUserRequest_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.createUserRequest(null, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void createUserRequest_NullCustomer_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.createUserRequest(validRequest, null)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void createUserRequest_DatabaseException_ThrowsException() {
        // Arrange
        when(userRequestRepository.save(any(UserRequest.class))).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                userRequestService.createUserRequest(validRequest, customer)
        );

        verify(userRequestRepository).save(any(UserRequest.class));
    }

    // GET USER REQUESTS TESTS

    @Test
    void getUserRequests_Success() {
        // Arrange
        List<UserRequest> userRequests = Collections.singletonList(sampleUserRequest);
        when(userRequestRepository.findByUserId(customerId)).thenReturn(userRequests);

        // Act
        GenericResponse<List<ViewUserRequestResponse>> response = userRequestService.getUserRequests(customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User requests retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(sampleUserRequest.getRequestId(), response.getData().get(0).getRequestId());

        verify(userRequestRepository).findByUserId(customerId);
    }

    @Test
    void getUserRequests_EmptyList_Success() {
        // Arrange
        List<UserRequest> emptyList = List.of();
        when(userRequestRepository.findByUserId(customerId)).thenReturn(emptyList);

        // Act
        GenericResponse<List<ViewUserRequestResponse>> response = userRequestService.getUserRequests(customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User requests retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(userRequestRepository).findByUserId(customerId);
    }

    @Test
    void getUserRequests_NullCustomer_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.getUserRequests(null)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void getUserRequests_DatabaseException_ThrowsException() {
        // Arrange
        when(userRequestRepository.findByUserId(customerId)).thenThrow(mock(DataAccessException.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                userRequestService.getUserRequests(customer)
        );

        verify(userRequestRepository).findByUserId(customerId);
    }

    // GET USER REQUEST BY ID TESTS

    @Test
    void getUserRequestById_Success() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));

        // Act
        GenericResponse<ViewUserRequestResponse> response = userRequestService.getUserRequestById(
                requestId.toString(), customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User request retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleUserRequest.getRequestId(), response.getData().getRequestId());

        verify(userRequestRepository).findById(requestId);
    }

    @Test
    void getUserRequestById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.getUserRequestById(null, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void getUserRequestById_InvalidUUID_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.getUserRequestById("invalid-uuid", customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void getUserRequestById_RequestNotFound_ThrowsException() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.getUserRequestById(requestId.toString(), customer)
        );

        verify(userRequestRepository).findById(requestId);
    }

    @Test
    void getUserRequestById_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
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

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.getUserRequestById(requestId.toString(), differentCustomer)
        );

        verify(userRequestRepository).findById(requestId);
    }

    // UPDATE USER REQUEST TESTS

    @Test
    void updateUserRequest_Success() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));
        when(userRequestRepository.save(any(UserRequest.class))).thenReturn(sampleUserRequest);

        // Act
        GenericResponse<ViewUserRequestResponse> response = userRequestService.updateUserRequest(
                requestId.toString(), validRequest, customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User request updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(sampleUserRequest.getRequestId(), response.getData().getRequestId());

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository).save(any(UserRequest.class));
    }

    @Test
    void updateUserRequest_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.updateUserRequest(null, validRequest, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void updateUserRequest_NullRequest_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.updateUserRequest(requestId.toString(), null, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void updateUserRequest_InvalidUUID_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.updateUserRequest("invalid-uuid", validRequest, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void updateUserRequest_RequestNotFound_ThrowsException() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.updateUserRequest(requestId.toString(), validRequest, customer)
        );

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository, never()).save(any(UserRequest.class));
    }

    @Test
    void updateUserRequest_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
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

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.updateUserRequest(requestId.toString(), validRequest, differentCustomer)
        );

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository, never()).save(any(UserRequest.class));
    }

    // DELETE USER REQUEST TESTS

    @Test
    void deleteUserRequest_Success() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));
        doNothing().when(userRequestRepository).delete(any(UserRequest.class));

        // Act
        GenericResponse<Void> response = userRequestService.deleteUserRequest(requestId.toString(), customer);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User request deleted successfully", response.getMessage());
        assertNull(response.getData());

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository).delete(sampleUserRequest);
    }

    @Test
    void deleteUserRequest_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.deleteUserRequest(null, customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void deleteUserRequest_NullCustomer_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.deleteUserRequest(requestId.toString(), null)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void deleteUserRequest_InvalidUUID_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.deleteUserRequest("invalid-uuid", customer)
        );

        verifyNoInteractions(userRequestRepository);
    }

    @Test
    void deleteUserRequest_RequestNotFound_ThrowsException() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(userRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.deleteUserRequest(requestId.toString(), customer)
        );

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository, never()).delete(any(UserRequest.class));
    }

    @Test
    void deleteUserRequest_UnauthorizedCustomer_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();
        UUID differentCustomerId = UUID.randomUUID();
        AuthenticatedUser differentCustomer = new AuthenticatedUser(
                differentCustomerId,
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

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));

        // Act & Assert
        assertThrows(InvalidUserRequestStateException.class, () ->
                userRequestService.deleteUserRequest(requestId.toString(), differentCustomer)
        );

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository, never()).delete(any(UserRequest.class));
    }

    @Test
    void deleteUserRequest_DatabaseException_ThrowsException() {
        // Arrange
        UUID requestId = sampleUserRequest.getRequestId();

        when(userRequestRepository.findById(requestId)).thenReturn(Optional.of(sampleUserRequest));
        doThrow(mock(DataAccessException.class)).when(userRequestRepository).delete(any(UserRequest.class));

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
                userRequestService.deleteUserRequest(requestId.toString(), customer)
        );

        verify(userRequestRepository).findById(requestId);
        verify(userRequestRepository).delete(sampleUserRequest);
    }
}