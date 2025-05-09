package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestDto;

import java.util.List;
import java.util.Optional;

public interface UserRequestService {
    ViewUserRequestDto createUserRequest(CreateAndUpdateUserRequestDto requestDto);
    List<ViewUserRequestDto> getAllUserRequests();
    Optional<ViewUserRequestDto> getUserRequestById(Long id);
    Optional<ViewUserRequestDto> updateUserRequest(Long id, CreateAndUpdateUserRequestDto requestDto);
    boolean deleteUserRequest(Long id);
}