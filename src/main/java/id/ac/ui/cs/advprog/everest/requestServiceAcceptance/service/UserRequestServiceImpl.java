package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRequestServiceImpl implements UserRequestService {

    private final UserRequestRepository repository;

    public UserRequestServiceImpl(UserRequestRepository repository) {
        this.repository = repository;
    }

    public UserRequest createRequest(String description) {
        UserRequest request = new UserRequest(null, description);
        return repository.save(request);
    }

    public UserRequest createRequest(UserRequest userRequest) {
        return repository.save(userRequest);
    }

    public Optional<UserRequest> getRequestById(Long id) {
        return repository.findById(id);
    }

    public List<UserRequest> getAllRequests() {
        return repository.findAll();
    }

    public boolean deleteRequestById(Long id) {
        return repository.deleteById(id);
    }

    public UserRequest createWithId(UserRequest request) {
        if (repository.findById(request.getId()).isPresent()) {
            throw new IllegalArgumentException("Duplicate ID");
        }
        return repository.save(request);
    }
}
