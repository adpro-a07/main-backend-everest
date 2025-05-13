package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponseDto;

import java.util.List;

public interface UserRequestService {
    GenericResponse<ViewUserRequestResponseDto> createUserRequest(
            CreateAndUpdateUserRequestDto createAndUpdateUserRequestDto,
            AuthenticatedUser customer
    );

    GenericResponse<List<ViewUserRequestResponseDto>> getUserRequests(AuthenticatedUser customer);

    GenericResponse<ViewUserRequestResponseDto> getUserRequestById(String requestId, AuthenticatedUser customer);

    GenericResponse<ViewUserRequestResponseDto> updateUserRequest(
            String requestId,
            CreateAndUpdateUserRequestDto createAndUpdateUserRequestDto,
            AuthenticatedUser customer
    );

    GenericResponse<Void> deleteUserRequest(String requestId, AuthenticatedUser customer);
}