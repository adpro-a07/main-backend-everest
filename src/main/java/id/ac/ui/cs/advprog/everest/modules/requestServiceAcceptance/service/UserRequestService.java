package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponse;

import java.util.List;

public interface UserRequestService {
    GenericResponse<ViewUserRequestResponse> createUserRequest(
            CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            AuthenticatedUser customer
    );

    GenericResponse<List<ViewUserRequestResponse>> getUserRequests(AuthenticatedUser customer);

    GenericResponse<ViewUserRequestResponse> getUserRequestById(String requestId, AuthenticatedUser customer);

    GenericResponse<ViewUserRequestResponse> updateUserRequest(
            String requestId,
            CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            AuthenticatedUser customer
    );

    GenericResponse<Void> deleteUserRequest(String requestId, AuthenticatedUser customer);
}