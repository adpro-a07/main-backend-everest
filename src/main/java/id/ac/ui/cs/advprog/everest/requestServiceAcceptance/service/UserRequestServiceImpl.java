package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRequestService {
    private final UserRequestRepository repository;

    public UserRequestService(UserRequestRepository repository) {
        this.repository = repository;
    }

    public UserRequest createRequest(UserRequest userRequest) {
        if (userRequest.getUserDescription() == null || userRequest.getUserDescription().isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        if (userRequest.getId() != null && repository.findById(userRequest.getId()).isPresent()) {
            throw new IllegalArgumentException("Request with this ID already exists");
        }

        return repository.save(userRequest);
    }

    
}
