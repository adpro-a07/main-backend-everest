package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;

import java.util.List;
import java.util.Optional;

public interface UserRequestService {
    UserRequest createRequest(String description);

    UserRequest createRequest(UserRequest userRequest);

    Optional<UserRequest> getRequestById(Long id);

    List<UserRequest> getAllRequests();

    boolean deleteRequestById(Long id);

    UserRequest createWithId(UserRequest request);
}
