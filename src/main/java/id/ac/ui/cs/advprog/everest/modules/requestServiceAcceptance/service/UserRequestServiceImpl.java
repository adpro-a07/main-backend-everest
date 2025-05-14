package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidUserRequestStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserRequestServiceImpl implements UserRequestService {
    private final UserRequestRepository userRequestRepository;

    public UserRequestServiceImpl(UserRequestRepository userRequestRepository) {
        this.userRequestRepository = userRequestRepository;
    }

    @Override
    public GenericResponse<ViewUserRequestResponse> createUserRequest(
            CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            AuthenticatedUser customer) {
        if (createAndUpdateUserRequestDto == null || customer == null) {
            throw new InvalidUserRequestStateException("Request or customer cannot be null");
        }

        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setUserId(customer.id());
            userRequest.setUserDescription(createAndUpdateUserRequestDto.getUserDescription());

            UserRequest savedRequest = userRequestRepository.save(userRequest);

            ViewUserRequestResponse responseView = buildUserRequestResponse(savedRequest);

            return new GenericResponse<>(true, "User request created successfully", responseView);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to save user request", ex);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidUserRequestStateException("Invalid data provided", ex);
        }
    }

    @Override
    public GenericResponse<List<ViewUserRequestResponse>> getUserRequests(AuthenticatedUser customer) {
        if (customer == null) {
            throw new InvalidUserRequestStateException("Customer cannot be null");
        }

        try {
            List<UserRequest> userRequests = userRequestRepository.findByUserId(customer.id());

            List<ViewUserRequestResponse> responseList = userRequests.stream()
                    .map(this::buildUserRequestResponse)
                    .toList();

            return new GenericResponse<>(true, "User requests retrieved successfully", responseList);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve user requests", ex);
        }
    }

    @Override
    public GenericResponse<ViewUserRequestResponse> getUserRequestById(String requestId, AuthenticatedUser customer) {
        if (requestId == null || customer == null) {
            throw new InvalidUserRequestStateException("Request ID or customer cannot be null");
        }

        try {
            UserRequest userRequest = userRequestRepository.findById(UUID.fromString(requestId))
                    .orElseThrow(() -> new InvalidUserRequestStateException("User request not found"));

            // Ensure the request belongs to the authenticated user
            if (!userRequest.getUserId().equals(customer.id())) {
                throw new InvalidUserRequestStateException("You are not authorized to view this request");
            }

            ViewUserRequestResponse responseView = buildUserRequestResponse(userRequest);

            return new GenericResponse<>(true, "User request retrieved successfully", responseView);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestStateException("Invalid request ID format", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve user request", ex);
        }
    }

    @Override
    public GenericResponse<ViewUserRequestResponse> updateUserRequest(
            String requestId,
            CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            AuthenticatedUser customer) {
        if (requestId == null || createAndUpdateUserRequestDto == null || customer == null) {
            throw new InvalidUserRequestStateException("Request ID, update data, or customer cannot be null");
        }

        try {
            UserRequest userRequest = userRequestRepository.findById(UUID.fromString(requestId))
                    .orElseThrow(() -> new InvalidUserRequestStateException("User request not found"));

            // Ensure the request belongs to the authenticated user
            if (!userRequest.getUserId().equals(customer.id())) {
                throw new InvalidUserRequestStateException("You are not authorized to update this request");
            }

            userRequest.setUserDescription(createAndUpdateUserRequestDto.getUserDescription());

            UserRequest updatedRequest = userRequestRepository.save(userRequest);

            ViewUserRequestResponse responseView = buildUserRequestResponse(updatedRequest);

            return new GenericResponse<>(true, "User request updated successfully", responseView);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestStateException("Invalid request ID format", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to update user request", ex);
        }
    }

    @Override
    public GenericResponse<Void> deleteUserRequest(String requestId, AuthenticatedUser customer) {
        if (requestId == null || customer == null) {
            throw new InvalidUserRequestStateException("Request ID or customer cannot be null");
        }

        try {
            UserRequest userRequest = userRequestRepository.findById(UUID.fromString(requestId))
                    .orElseThrow(() -> new InvalidUserRequestStateException("User request not found"));

            // Ensure the request belongs to the authenticated user
            if (!userRequest.getUserId().equals(customer.id())) {
                throw new InvalidUserRequestStateException("You are not authorized to delete this request");
            }

            userRequestRepository.delete(userRequest);

            return new GenericResponse<>(true, "User request deleted successfully", null);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestStateException("Invalid request ID format", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to delete user request", ex);
        }
    }

    private ViewUserRequestResponse buildUserRequestResponse(UserRequest userRequest) {
        return ViewUserRequestResponse.builder()
                .requestId(userRequest.getRequestId())
                .userId(userRequest.getUserId())
                .userDescription(userRequest.getUserDescription())
                .createdAt(LocalDateTime.now()) // Note: You should add these fields to the entity
                .updatedAt(LocalDateTime.now())
                .build();
    }
}